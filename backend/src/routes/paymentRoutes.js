import express from 'express';
import WebhookController from '../controllers/WebhookController.js';

const router = express.Router();

/**
 * @route   POST /api/v1/payments/webhook
 * @desc    Handle Razorpay Webhooks
 * @access  Public (Signature verified internally)
 */
router.post('/webhook', WebhookController.handleWebhook.bind(WebhookController));

export default router;
