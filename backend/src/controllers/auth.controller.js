import User from '../models/User.js';

// Get token from model, create cookie and send response
const sendTokenResponse = (user, statusCode, res) => {
  const token = user.getSignedJwtToken();

  res.status(statusCode).json({
    success: true,
    token,
    user: {
      id: user._id,
      name: user.name,
      email: user.email,
      role: user.role,
    },
  });
};

// @desc    Register a user or provider
// @route   POST /api/v1/auth/register
// @access  Public
export const register = async (req, res, next) => {
  try {
    const { name, email, password, role } = req.body;

    // Security protocol: Normal users cannot register as 'admin' directly.
    const assignedRole = role === 'provider' ? 'provider' : 'user';

    // Create user
    const user = await User.create({
      name,
      email,
      password,
      role: assignedRole,
    });

    sendTokenResponse(user, 201, res);
  } catch (error) {
    next(error);
  }
};

// @desc    Login user
// @route   POST /api/v1/auth/login
// @access  Public
export const login = async (req, res, next) => {
  try {
    const { email, password } = req.body;

    // Validate email & password
    if (!email || !password) {
      return res.status(400).json({ success: false, error: 'Please provide an email and password' });
    }

    // Check for user
    const user = await User.findOne({ email }).select('+password');

    if (!user) {
      return res.status(401).json({ success: false, error: 'Invalid credentials' });
    }

    // Check if password matches
    const isMatch = await user.matchPassword(password);

    if (!isMatch) {
      return res.status(401).json({ success: false, error: 'Invalid credentials' });
    }

    sendTokenResponse(user, 200, res);
  } catch (error) {
    next(error);
  }
};

// @desc    Get current logged in user
// @route   GET /api/v1/auth/me
// @access  Private
export const getMe = async (req, res, next) => {
  try {
    const user = await User.findById(req.user.id);
    res.status(200).json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
};

// ---------------------------------------------------------
// PHASE 2 TEST CONTROLLERS
// ---------------------------------------------------------

export const testUser = (req, res) => {
  res.status(200).json({
    success: true,
    message: 'User access granted. Only authenticated users can see this.',
    user: req.user
  });
};

export const testProvider = (req, res) => {
  res.status(200).json({
    success: true,
    message: 'Provider access granted. You can add services here.',
    user: req.user
  });
};

export const testAdmin = (req, res) => {
  res.status(200).json({
    success: true,
    message: 'Admin access granted. You have full system control.',
    user: req.user
  });
};
