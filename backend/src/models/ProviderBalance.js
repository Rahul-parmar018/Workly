import mongoose from 'mongoose';

const ProviderBalanceSchema = new mongoose.Schema(
  {
    provider: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Provider',
      required: true,
      unique: true,
    },
    pendingEscrow: { type: Number, default: 0 }, // Held in Razorpay Route
    availableForPayout: { type: Number, default: 0 }, // Released but not settled
    negativeBalance: { type: Number, default: 0 }, // For handling post-payout refunds
    totalEarnings: { type: Number, default: 0 },
    currency: { type: String, default: 'INR' },
  },
  { timestamps: true }
);

const ProviderBalance = mongoose.model(
  'ProviderBalance',
  ProviderBalanceSchema
);
export default ProviderBalance;
