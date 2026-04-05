import express from 'express';
import WebhookController from '../controllers/WebhookController.js';
import ProviderPaymentController from '../controllers/ProviderPaymentController.js';
import { protect } from '../middleware/auth.middleware.js'; // Assuming this exists or using a generic protection

const router = express.Router();

/**
 * @route   POST /api/v1/payments/webhook
 * @desc    Handle Razorpay Webhooks
 * @access  Public (Signature verified internally)
 */
router.post('/webhook', WebhookController.handleWebhook.bind(WebhookController));

/**
 * Provider Payment Routes
 */
router.get('/provider/wallet', protect, ProviderPaymentController.getWalletOverview.bind(ProviderPaymentController));
router.get('/provider/transactions', protect, ProviderPaymentController.getTransactionHistory.bind(ProviderPaymentController));
router.post('/provider/withdraw', protect, ProviderPaymentController.requestWithdrawal.bind(ProviderPaymentController));

export default router;
