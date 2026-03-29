import express from 'express';
import AdminController from '../controllers/AdminController.js';

const router = express.Router();

/**
 * @route   POST /api/v1/admin/disputes/:bookingId/flag
 * @desc    Flag a booking as disputed
 * @access  Private/Admin
 */
router.post('/disputes/:bookingId/flag', AdminController.flagDispute.bind(AdminController));

/**
 * @route   POST /api/v1/admin/disputes/:bookingId/resolve
 * @desc    Resolve a dispute (Release or Refund)
 * @access  Private/Admin
 */
router.post('/disputes/:bookingId/resolve', AdminController.resolveDispute.bind(AdminController));

export default router;
