import RazorpayService from '../services/RazorpayService.js';
import Booking from '../models/Booking.js';
import Payment from '../models/Payment.js';
import Payout from '../models/Payout.js';
import Provider from '../models/Provider.js';
import TransactionAudit from '../models/TransactionAudit.js';

class WebhookController {
  /**
   * Main Webhook Handler
   */
  async handleWebhook(req, res) {
    const signature = req.headers['x-razorpay-signature'];
    const body = req.body;

    // 1. Verify Signature
    if (!RazorpayService.verifyWebhookSignature(body, signature)) {
      return res.status(400).send('Invalid signature');
    }

    const event = body.event;
    const payload = body.payload;

    try {
      switch (event) {
        case 'payment.captured':
          await this.processPaymentCaptured(payload.payment.entity);
          break;
        case 'payment.failed':
          await this.processPaymentFailed(payload.payment.entity);
          break;
        case 'refund.processed':
          await this.processRefund(payload.refund.entity);
          break;
        case 'transfer.processed':
          await this.updatePayoutStatus(payload.transfer.entity, 'RELEASED');
          break;
        case 'transfer.failed':
          await this.updatePayoutStatus(payload.transfer.entity, 'FAILED');
          break;
      }

      res.status(200).json({ status: 'ok' });
    } catch (error) {
      console.error('Webhook processing error:', error);
      res.status(500).json({ error: error.message });
    }
  }

  /**
   * Handle payment.captured
   * Logic: Update Booking to CONFIRMED + Immediate Transfer with ON_HOLD
   */
  async processPaymentCaptured(paymentEntity) {
    const { order_id, id: paymentId, amount, notes } = paymentEntity;
    const bookingId = notes.bookingId;

    const booking = await Booking.findById(bookingId).populate('provider');
    if (!booking) throw new Error('Booking not found');

    // Atomic update status
    booking.status = 'CONFIRMED';
    await booking.save();

    const payment = await Payment.findOneAndUpdate(
      { razorpayOrderId: order_id },
      { razorpayPaymentId: paymentId, status: 'CAPTURED' },
      { new: true }
    );

    // Immediate Escrow Transfer
    const provider = booking.provider;
    const commission = (amount / 100) * (provider.commissionRate / 100);
    const netAmount = amount / 100 - commission;

    const transfer = await RazorpayService.transferToProvider(
      payment._id,
      provider.razorpayAccountId,
      netAmount,
      booking._id
    );

    // Create Payout Record
    await Payout.create({
      payment: payment._id,
      provider: provider._id,
      transferId: transfer.id,
      amount: netAmount,
      status: 'ON_HOLD',
    });

    // Update Provider Balance Ledger
    await ProviderBalance.findOneAndUpdate(
      { provider: provider._id },
      { $inc: { pendingEscrow: netAmount } },
      { upsert: true }
    );

    await TransactionAudit.create({
      entityType: 'Booking',
      entityId: booking._id,
      fromStatus: 'PENDING_PAYMENT',
      toStatus: 'CONFIRMED',
      action: 'PAYMENT_CAPTURED_WEBHOOK',
    });
  }

  async processPaymentFailed(paymentEntity) {
    const bookingId = paymentEntity.notes.bookingId;
    await Booking.findByIdAndUpdate(bookingId, { status: 'PAYMENT_FAILED' });
  }

  async updatePayoutStatus(transferEntity, status) {
    await Payout.findOneAndUpdate(
      { transferId: transferEntity.id },
      { status }
    );
  }
}

export default new WebhookController();
