import mongoose from 'mongoose';
import ProviderBalance from '../models/ProviderBalance.js';
import Payout from '../models/Payout.js';
import TransactionAudit from '../models/TransactionAudit.js';
import { addPayoutToQueue } from '../services/payoutQueue.js';
import ReconciliationService from '../services/ReconciliationService.js';
import Provider from '../models/Provider.js';

class ProviderPaymentController {
  /**
   * Get Wallet Overview for Provider
   * Returns available balance, pending escrow, and recent transactions.
   */
  async getWalletOverview(req, res) {
    try {
      const userId = req.user._id;
      const provider = await Provider.findOne({ user: userId });
      
      if (!provider) {
        return res.status(404).json({ error: 'Provider record not found' });
      }

      const providerId = provider._id;
      const balance = await ProviderBalance.findOne({ provider: providerId });
      
      if (!balance) {
        return res.status(200).json({
          availableForPayout: 0,
          pendingEscrow: 0,
          totalEarnings: 0,
          currency: 'INR'
        });
      }

      // Fetch recent payouts
      const recentTransactions = await Payout.find({ provider: providerId })
        .sort({ createdAt: -1 })
        .limit(10);

      const normalizedTransactions = recentTransactions.map(t => ({
        type: 'WITHDRAWAL',
        amount: t.amount,
        status: t.status,
        createdAt: t.createdAt,
        referenceId: t.requestId
      }));

      res.status(200).json({
        availableForPayout: balance.availableForPayout,
        pendingEscrow: balance.pendingEscrow,
        totalEarnings: balance.totalEarnings,
        currency: balance.currency,
        transactions: normalizedTransactions
      });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }

  /**
   * Request a Withdrawal (Atomic Transactional Outbox)
   */
  async requestWithdrawal(req, res) {
    const { amount, requestId, payoutMethod } = req.body;
    const userId = req.user._id;
    const provider = await Provider.findOne({ user: userId });

    if (!provider) {
      return res.status(404).json({ error: 'Provider record not found' });
    }

    const providerId = provider._id;

    if (!amount || amount < 100) {
      return res.status(400).json({ error: 'Minimum withdrawal amount is ₹100' });
    }

    const session = await mongoose.startSession();
    session.startTransaction();

    try {
      // 1. Check Idempotency
      const existingPayout = await Payout.findOne({ requestId }).session(session);
      if (existingPayout) {
        await session.abortTransaction();
        return res.status(200).json(existingPayout);
      }

      // 2. Fetch Balance and Validate
      const balance = await ProviderBalance.findOne({ provider: providerId }).session(session);
      if (!balance || balance.availableForPayout < amount) {
        throw new Error('Insufficient available balance for withdrawal.');
      }

      // 3. atomic updates
      // Decrement balance
      balance.availableForPayout -= amount;
      await balance.save({ session });

      // Create Payout Record (enqueued: false)
      const payout = await Payout.create([{
        requestId,
        provider: providerId,
        amount,
        status: 'REQUESTED',
        enqueued: false,
        payoutMethod
      }], { session });

      // Create Audit Log
      await TransactionAudit.create([{
        entityType: 'Payout',
        entityId: payout[0]._id,
        toStatus: 'REQUESTED',
        action: 'MANUAL_WITHDRAWAL_REQUEST',
        performedBy: 'PROVIDER'
      }], { session });

      // COMMIT TRANSACTION
      await session.commitTransaction();
      session.endSession();

      // 4. Try Enqueueing (Outbox Pattern)
      try {
        await addPayoutToQueue(payout[0]._id.toString());
        await Payout.updateOne({ _id: payout[0]._id }, { enqueued: true });
      } catch (enqueueError) {
        console.error('[OUTBOX] Failed to enqueue payout after commit. Recovery job will pick it up.', enqueueError);
      }

      res.status(201).json(payout[0]);
    } catch (error) {
      await session.abortTransaction();
      session.endSession();
      res.status(400).json({ error: error.message });
    }
  }

  /**
   * Comprehensive Transaction History (Normalized)
   */
  async getTransactionHistory(req, res) {
    try {
      const userId = req.user._id;
      const provider = await Provider.findOne({ user: userId });

      if (!provider) {
        return res.status(404).json({ error: 'Provider record not found' });
      }

      const providerId = provider._id;
      
      // Use aggregation to unify Payments (Earnings) and Payouts (Withdrawals)
      const history = await Payout.aggregate([
        { $match: { provider: new mongoose.Types.ObjectId(providerId) } },
        { $project: { 
            type: { $literal: 'WITHDRAWAL' },
            amount: 1,
            status: 1,
            createdAt: 1,
            referenceId: '$requestId'
        }},
        { $unionWith: {
            coll: 'payments',
            pipeline: [
              // In this project, payments are linked to bookings, which are linked to providers.
              // This part requires a join if 'payments' don't have direct provider links.
              // For simplicity in this demo, we assume we fetch payments linked to provider's bookings.
              { $lookup: {
                  from: 'bookings',
                  localField: 'booking',
                  foreignField: '_id',
                  as: 'booking_info'
              }},
              { $unwind: '$booking_info' },
              { $match: { 'booking_info.provider': new mongoose.Types.ObjectId(providerId) } },
              { $project: {
                  type: { $literal: 'EARNING' },
                  amount: { $divide: ['$amount', 100] }, // Convert paise to INR
                  status: 1,
                  createdAt: 1,
                  referenceId: '$razorpayPaymentId'
              }}
            ]
        }},
        { $sort: { createdAt: -1 } }
      ]);

      res.status(200).json(history);
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }
}

export default new ProviderPaymentController();
