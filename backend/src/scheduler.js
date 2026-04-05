import cron from 'node-cron';
import PayoutService from './services/PayoutService.js';

/**
 * Initialize all background jobs
 */
const initSchedulers = () => {
  // Run every hour to check for matured escrow payouts
  cron.schedule('0 * * * *', async () => {
    console.log('[CRON] Starting automated payout release scan...');
    await PayoutService.processAutomatedReleases();
  });

  // Run reconciliation job at 3 AM daily
  cron.schedule('0 3 * * *', async () => {
    console.log('[CRON] Starting daily payment reconciliation...');
    // TODO: Implement ReconciliationService.syncTransactions()
  });
};

export default initSchedulers;
