import Razorpay from 'razorpay';
import crypto from 'crypto';

class RazorpayService {
  constructor() {
    this.razorpay = new Razorpay({
      key_id: process.env.RAZORPAY_KEY_ID,
      key_secret: process.env.RAZORPAY_KEY_SECRET,
    });
  }

  /**
   * Create a Razorpay Order
   */
  async createOrder(amount, receipt, notes = {}) {
    return await this.razorpay.orders.create({
      amount: amount * 100, // in paise
      currency: 'INR',
      receipt,
      notes,
    });
  }

  /**
   * Immediate Transfer to Provider (with on_hold: true)
   */
  async transferToProvider(paymentId, providerAccountId, amount, bookingId) {
    return await this.razorpay.transfers.create({
      account: providerAccountId,
      amount: amount * 100, // in paise
      currency: 'INR',
      on_hold: true,
      notes: {
        bookingId: bookingId.toString(),
        paymentId: paymentId.toString(),
      },
    });
  }

  /**
   * Release Hold on a Transfer
   */
  async releaseHold(transferId) {
    return await this.razorpay.transfers.edit(transferId, {
      on_hold: false,
    });
  }

  /**
   * Handle Refund (Escrow Stage or Post-Payout)
   */
  async createRefund(paymentId, amount, speed = 'normal') {
    return await this.razorpay.payments.refund(paymentId, {
      amount: amount * 100,
      speed,
    });
  }

  /**
   * Verify Webhook Signature
   */
  verifyWebhookSignature(body, signature) {
    const expectedSignature = crypto
      .createHmac('sha256', process.env.RAZORPAY_WEBHOOK_SECRET)
      .update(JSON.stringify(body))
      .digest('hex');
    return expectedSignature === signature;
  }
}

export default new RazorpayService();
