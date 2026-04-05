import Payout from '../models/Payout.js';
import { addPayoutToQueue } from './payoutQueue.js';

/**
 * Outbox Recovery Service
 * Ensures reliability by detecting missed queue submissions and stuck payouts.
 */
class OutboxRecoveryService {
    /**
     * Finds and re-enqueues MISSED payouts (Outbox pattern)
     * and stuck PROCESSING payouts (Fault tolerance).
     */
    async recover() {
        console.log('[RECOVERY] Starting outbox recovery scan...');
        
        // 1. Recover MISSED payouts (committed to DB but failed to enqueue)
        // Logic: status is REQUESTED and enqueued is false
        const missedPayouts = await Payout.find({
            status: 'REQUESTED',
            enqueued: false,
        });

        for (const payout of missedPayouts) {
            try {
                console.log(`[RECOVERY] Re-enqueuing missed payout: ${payout._id}`);
                await addPayoutToQueue(payout._id.toString());
                payout.enqueued = true;
                await payout.save();
            } catch (err) {
                console.error(`[RECOVERY] Failed to re-enqueue ${payout._id}:`, err);
            }
        }

        // 2. Recover STUCK processing payouts
        // Logic: status is PROCESSING and processingStartedAt > 1 hour ago
        const oneHourAgo = new Date();
        oneHourAgo.setHours(oneHourAgo.getHours() - 1);

        const stuckPayouts = await Payout.find({
            status: 'PROCESSING',
            processingStartedAt: { $lt: oneHourAgo },
        });

        for (const payout of stuckPayouts) {
            try {
                console.warn(`[RECOVERY] Resetting stuck payout: ${payout._id}`);
                
                // Reset to REQUESTED so the queue can pick it up again
                payout.status = 'REQUESTED';
                payout.enqueued = false;
                payout.failureReason = 'STUCK_IN_PROCESSING_RESET';
                payout.failureLog.push({ error: 'System automatically reset stuck processing payout.' });
                await payout.save();

                // Immediately re-enqueue
                await addPayoutToQueue(payout._id.toString());
                payout.enqueued = true;
                await payout.save();
            } catch (err) {
                console.error(`[RECOVERY] Failed to reset stuck payout ${payout._id}:`, err);
            }
        }

        console.log(`[RECOVERY] Scan complete. Found ${missedPayouts.length} missed, ${stuckPayouts.length} stuck.`);
    }
}

export default new OutboxRecoveryService();
