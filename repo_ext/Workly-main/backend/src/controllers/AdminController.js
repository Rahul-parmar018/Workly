import Booking from '../models/Booking.js';
import Payout from '../models/Payout.js';
import ProviderBalance from '../models/ProviderBalance.js';
import TransactionAudit from '../models/TransactionAudit.js';
import RazorpayService from '../services/RazorpayService.js';

class AdminController {
  /**
   * Raised when a user contests a job
   */
  async flagDispute(req, res) {
    const { bookingId } = req.params;
    const { reason } = req.body;

    const booking = await Booking.findById(bookingId);
    if (!booking) return res.status(404).json({ error: 'Booking not found' });

    booking.status = 'DISPUTED';
    await booking.save();

    // Pause payout if not already released
    await Payout.findOneAndUpdate(
      { booking: bookingId, status: 'ON_HOLD' },
      { failureReason: `DISPUTE_RAISED: ${reason}` }
    );

    await TransactionAudit.create({
      entityType: 'Booking',
      entityId: bookingId,
      toStatus: 'DISPUTED',
      action: 'ADMIN_DISPUTE_FLAG',
      metadata: { reason },
    });

    res.status(200).json({ success: true, message: 'Payout paused. Under investigation.' });
  }

  /**
   * Resolve dispute: Either release to provider or refund to user
   */
  async resolveDispute(req, res) {
    const { bookingId } = req.params;
    const { decision } = req.body; // 'RELEASE' or 'REFUND'

    const payout = await Payout.findOne({ booking: bookingId });
    
    if (decision === 'RELEASE') {
      await RazorpayService.releaseHold(payout.transferId);
      payout.status = 'RELEASED';
    } else {
      // Logic for Refund
      const payment = await Payment.findById(payout.payment);
      await RazorpayService.createRefund(payment.razorpayPaymentId, payout.amount);
      payout.status = 'REVERSED';
    }

    await payout.save();
    res.status(200).json({ success: true });
  }
}

export default new AdminController();
