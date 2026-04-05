import { Worker } from 'bullmq';
import Payout from '../models/Payout.js';
import ProviderBalance from '../models/ProviderBalance.js';
import TransactionAudit from '../models/TransactionAudit.js';
import RazorpayService from './RazorpayService.js';
import dotenv from 'dotenv';
import mongoose from 'mongoose';

dotenv.config();

/**
 * Payout Worker
 * Processes withdrawal requests asynchronously with atomic state transitions.
 */
export const payoutWorker = new Worker(
  'payout-queue',
  async (job) => {
    const { payoutId } = job.data;
    
    // 1. Atomic state transition: REQUESTED -> PROCESSING
    // This provides worker-level idempotency
    const payout = await Payout.findOneAndUpdate(
      { _id: payoutId, status: 'REQUESTED' },
      { status: 'PROCESSING', processingStartedAt: new Date() },
      { new: true }
    );

    if (!payout) {
      console.log(`[Worker] Payout ${payoutId} already processing or completed.`);
      return;
    }

    try {
      console.log(`[Worker] Processing payout ${payoutId} for ₹${payout.amount}...`);

      // 2. Perform the actual payout logic (simulated or Razorpay Payouts)
      // For now, we simulate success after a small delay
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // Optional: Store external payout reference
      const externalPayoutId = `ext_${Date.now()}_${payoutId.substring(0, 8)}`;
      
      // 3. Update Payout record on success
      payout.status = 'SUCCESS';
      payout.externalPayoutId = externalPayoutId;
      payout.releasedAt = new Date();
      await payout.save();

      // 4. Finalize Audit Log
      await TransactionAudit.create({
        entityType: 'Payout',
        entityId: payout._id,
        fromStatus: 'PROCESSING',
        toStatus: 'SUCCESS',
        action: 'WORKER_PAYOUT_SUCCESS',
        metadata: { externalPayoutId },
      });

      console.log(`[Worker] Payout ${payoutId} completed successfully.`);
    } catch (error) {
      console.error(`[Worker] Payout ${payoutId} failed:`, error);

      // Handle Failure and Retries
      payout.status = 'FAILED';
      payout.failureReason = error.message;
      payout.failureLog.push({ error: error.message });
      payout.retryCount += 1;
      
      // Implement capped retry strategy: retry after 1h, 2h, 4h, etc.
      if (payout.retryCount <= 5) {
        const nextRetry = new Date();
        nextRetry.setHours(nextRetry.getHours() + Math.pow(2, payout.retryCount - 1));
        payout.nextRetryAt = nextRetry;
      }
      
      await payout.save();

      await TransactionAudit.create({
        entityType: 'Payout',
        entityId: payout._id,
        fromStatus: 'PROCESSING',
        toStatus: 'FAILED',
        action: 'WORKER_PAYOUT_FAILURE',
        metadata: { error: error.message, retryCount: payout.retryCount },
      });

      throw error; // Let BullMQ handle re-queueing based on job options
    }
  },
  {
    connection: {
      host: process.env.REDIS_HOST || '127.0.0.1',
      port: parseInt(process.env.REDIS_PORT || '6379'),
    },
    concurrency: 5, // Limit concurrent processing
  }
);

export default payoutWorker;
