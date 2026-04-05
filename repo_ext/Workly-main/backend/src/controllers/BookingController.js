import Booking from '../models/Booking.js';
import PayoutService from '../services/PayoutService.js';
import TransactionAudit from '../models/TransactionAudit.js';

class BookingController {
  /**
   * Provider marks job as completed
   */
  async completeBooking(req, res) {
    const { bookingId } = req.params;

    try {
      const booking = await Booking.findById(bookingId);
      if (!booking) return res.status(404).json({ error: 'Booking not found' });

      // State Transition
      const oldStatus = booking.status;
      booking.status = 'COMPLETED';
      booking.completedAt = new Date();
      await booking.save();

      // Trigger the 24-hour escrow release timer
      await PayoutService.scheduleRelease(booking._id, 24);

      await TransactionAudit.create({
        entityType: 'Booking',
        entityId: booking._id,
        fromStatus: oldStatus,
        toStatus: 'COMPLETED',
        action: 'PROVIDER_ACTION_COMPLETE',
      });

      res.status(200).json({ success: true, message: 'Booking completed. Funds will release in 24h.' });
    } catch (error) {
      res.status(500).json({ error: error.message });
    }
  }

  /**
   * Provider starts work
   */
  async startBooking(req, res) {
    const { bookingId } = req.params;
    await Booking.findByIdAndUpdate(bookingId, { status: 'IN_PROGRESS' });
    res.status(200).json({ success: true });
  }
}

export default new BookingController();
