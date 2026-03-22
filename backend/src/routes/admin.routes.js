import express from 'express';
import { getPendingProviders, approveProvider } from '../controllers/admin.controller.js';
import { protect, authorize } from '../middleware/auth.middleware.js';

const router = express.Router();

// Apply auth protecting and admin-only role to all routing below
router.use(protect);
router.use(authorize('admin'));

router.get('/providers/pending', getPendingProviders);
router.put('/providers/:id/approve', approveProvider);

export default router;
