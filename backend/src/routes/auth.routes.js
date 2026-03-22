import express from 'express';
import { 
  register, 
  login, 
  getMe,
  testUser,
  testProvider,
  testAdmin
} from '../controllers/auth.controller.js';
import { protect, authorize } from '../middleware/auth.middleware.js';

const router = express.Router();

// Public Authentication Routes
router.post('/register', register);
router.post('/login', login);

// Protected Core Route
router.get('/me', protect, getMe);

// Protected Role-Test Routes (Phase 2 enforcement)
router.get('/test/user', protect, authorize('user', 'provider', 'admin'), testUser);
router.get('/test/provider', protect, authorize('provider', 'admin'), testProvider);
router.get('/test/admin', protect, authorize('admin'), testAdmin);

export default router;
