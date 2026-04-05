import cron from 'node-cron';
import PayoutService from './services/PayoutService.js';
import OutboxRecoveryService from './services/OutboxRecoveryService.js';
import ReconciliationService from './services/ReconciliationService.js';

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
    await ReconciliationService.syncTransactions();
  });

  // Run every 15 minutes to recover missed or stuck payouts
  cron.schedule('*/15 * * * *', async () => {
    console.log('[CRON] Starting outbox recovery job...');
    await OutboxRecoveryService.recover();
  });
};

export default initSchedulers;
