import mongoose from 'mongoose';

const ProviderSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    razorpayAccountId: {
      type: String,
      unique: true,
      sparse: true, // Only if they have completed onboarding
    },
    businessName: String,
    category: {
      type: String,
      required: true,
    },
    commissionRate: {
      type: Number,
      default: 10, // Default 10%
    },
    balance: {
      pending: { type: Number, default: 0 },
      available: { type: Number, default: 0 },
    },
    status: {
      type: String,
      enum: ['active', 'suspended', 'onboarding'],
      default: 'active',
    },
    kycStatus: {
      type: String,
      enum: ['pending', 'verified', 'rejected'],
      default: 'pending',
    },
  },
  { timestamps: true }
);

const Provider = mongoose.model('Provider', ProviderSchema);
export default Provider;
