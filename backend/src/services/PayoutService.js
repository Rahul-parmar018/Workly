import Payout from '../models/Payout.js';
import Booking from '../models/Booking.js';
import RazorpayService from './RazorpayService.js';
import TransactionAudit from '../models/TransactionAudit.js';

class PayoutService {
  /**
   * Scans for Payouts that are ready to be released
   * Logic: onHoldUntil < now AND Booking.status == COMPLETED
   */
  async processAutomatedReleases() {
    const now = new Date();

    // Find all Payouts that are ON_HOLD and due to be released
    const readyPayouts = await Payout.find({
      status: 'ON_HOLD',
      onHoldUntil: { $lte: now },
    }).populate('booking');

    for (const payout of readyPayouts) {
      // Safety check: Only release if booking is COMPLETED and not DISPUTED
      if (payout.booking.status === 'COMPLETED') {
        try {
          await RazorpayService.releaseHold(payout.transferId);
          
          payout.status = 'RELEASED';
          payout.releasedAt = now;
          await payout.save();

          // Update Balance Ledger: Move from Pending to Available
          await ProviderBalance.findOneAndUpdate(
            { provider: payout.provider },
            { 
              $inc: { 
                pendingEscrow: -payout.amount,
                availableForPayout: payout.amount,
                totalEarnings: payout.amount 
              } 
            }
          );

          await TransactionAudit.create({
            entityType: 'Payout',
            entityId: payout._id,
            fromStatus: 'ON_HOLD',
            toStatus: 'RELEASED',
            action: 'AUTOMATED_RELEASE',
          });
        } catch (error) {
          console.error(`Failed to release payout ${payout._id}:`, error);
          payout.failureReason = error.message;
          payout.retryCount += 1;
          await payout.save();
        }
      }
    }
  }

  /**
   * Helper to set release window on job completion
   */
  async scheduleRelease(bookingId, windowHours = 24) {
    const releaseTime = new Date();
    releaseTime.setHours(releaseTime.getHours() + windowHours);

    await Payout.findOneAndUpdate(
      { booking: bookingId },
      { onHoldUntil: releaseTime }
    );
  }
}

export default new PayoutService();
