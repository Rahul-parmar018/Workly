import User from '../models/User.js';

// @desc    Get all pending providers
// @route   GET /api/v1/admin/providers/pending
// @access  Private/Admin
export const getPendingProviders = async (req, res, next) => {
  try {
    const providers = await User.find({ role: 'provider', isApproved: false });
    res.status(200).json({ success: true, count: providers.length, data: providers });
  } catch (error) {
    next(error);
  }
};

// @desc    Approve a provider
// @route   PUT /api/v1/admin/providers/:id/approve
// @access  Private/Admin
export const approveProvider = async (req, res, next) => {
  try {
    const provider = await User.findById(req.params.id);

    if (!provider) {
      return res.status(404).json({ success: false, error: 'Provider not found' });
    }

    if (provider.role !== 'provider') {
      return res.status(400).json({ success: false, error: 'User is not a provider' });
    }

    provider.isApproved = true;
    await provider.save();

    res.status(200).json({ success: true, data: provider });
  } catch (error) {
    next(error);
  }
};
