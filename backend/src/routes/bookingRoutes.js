import express from 'express';
import BookingController from '../controllers/BookingController.js';

const router = express.Router();

/**
 * @route   POST /api/v1/bookings/:bookingId/complete
 * @desc    Mark booking as completed (Starts escrow window)
 * @access  Private/Provider
 */
router.post('/:bookingId/complete', BookingController.completeBooking.bind(BookingController));

/**
 * @route   POST /api/v1/bookings/:bookingId/start
 * @desc    Provider starts service
 * @access  Private/Provider
 */
router.post('/:bookingId/start', BookingController.startBooking.bind(BookingController));

export default router;
