import ProviderBalance from '../models/ProviderBalance.js';
import Payout from '../models/Payout.js';
import Payment from '../models/Payment.js';

/**
 * Reconciliation Service
 * Enforces financial integrity and the Ledger Invariant across all providers.
 */
class ReconciliationService {
    /**
     * Ledger Invariant: totalEarnings = availableForPayout + pendingEscrow - negativeBalance
     * This method scans all providers and verifies the invariant holds.
     */
    async syncTransactions() {
        console.log('[RECON] Starting global ledger reconciliation...');
        const balances = await ProviderBalance.find().populate('provider');
        
        let discrepancies = 0;

        for (const balance of balances) {
            const { 
                totalEarnings, 
                availableForPayout, 
                pendingEscrow, 
                negativeBalance,
                provider 
            } = balance;

            // 1. Calculate the expected earnings based on the invariant formula
            const expectedEarnings = availableForPayout + pendingEscrow - negativeBalance;

            // 2. Delta Check
            const delta = Math.abs(totalEarnings - expectedEarnings);

            if (delta > 0.01) { // Floating point safety margin
                discrepancies++;
                console.error(`[RECON] INVARIANT VIOLATION for provider ${provider?.name} (${provider?._id})`);
                console.error(`[RECON] Expected Earnings: ${expectedEarnings}, Actual: ${totalEarnings}, Delta: ${delta}`);
                
                // TODO: Automated Fix or Flagging
            }
        }

        console.log(`[RECON] Global scan complete. Found ${discrepancies} discrepancies.`);
    }

    /**
     * Helper to verify a single provider's ledger health
     * @param {string} providerId 
     */
    async verifyProviderLedger(providerId) {
        const balance = await ProviderBalance.findOne({ provider: providerId });
        if (!balance) return true;

        const expectedEarnings = balance.availableForPayout + balance.pendingEscrow - balance.negativeBalance;
        return Math.abs(balance.totalEarnings - expectedEarnings) <= 0.01;
    }
}

export default new ReconciliationService();
