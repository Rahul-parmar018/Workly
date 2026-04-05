import { Queue } from 'bullmq';
import dotenv from 'dotenv';

dotenv.config();

/**
 * Payout Queue Configuration
 * This queue handles the asynchronous processing of provider withdrawals.
 */
export const payoutQueue = new Queue('payout-queue', {
  connection: {
    host: process.env.REDIS_HOST || '127.0.0.1',
    port: parseInt(process.env.REDIS_PORT || '6379'),
  },
  defaultJobOptions: {
    attempts: 5,
    backoff: {
      type: 'exponential',
      delay: 5000,
    },
    removeOnComplete: true,
    removeOnFail: false,
  },
});

/**
 * Adds a payout to the processing queue
 * @param {string} payoutId - MongoDB ID of the payout record
 */
export const addPayoutToQueue = async (payoutId) => {
  await payoutQueue.add('process-payout', { payoutId }, {
    jobId: payoutId, // Enforce job-level idempotency in the queue
  });
};

export default payoutQueue;
