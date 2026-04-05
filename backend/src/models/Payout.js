import mongoose from 'mongoose';

const PayoutSchema = new mongoose.Schema(
  {
    requestId: {
      type: String, // UUID for idempotency
      required: true,
      unique: true,
    },
    payment: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Payment',
    },
    provider: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Provider',
      required: true,
    },
    transferId: {
      type: String, // Razorpay Transfer ID (optional if manual payout)
      sparse: true,
      unique: true,
    },
    externalPayoutId: {
      type: String, // Bank/Razorpay payout reference
      sparse: true,
      unique: true,
    },
    amount: {
      type: Number,
      required: true,
    },
    status: {
      type: String,
      enum: ['REQUESTED', 'PROCESSING', 'SUCCESS', 'FAILED', 'CANCELLED'],
      default: 'REQUESTED',
    },
    enqueued: {
      type: Boolean,
      default: false,
    },
    processingStartedAt: Date,
    onHoldUntil: Date,
    releaseAt: Date, // Window expiry time
    releasedAt: Date, // Real release time
    failureReason: String,
    failureLog: [
      {
        timestamp: { type: Date, default: Date.now },
        error: String,
      },
    ],
    retryCount: {
      type: Number,
      default: 0,
    },
    nextRetryAt: Date,
  },
  { timestamps: true }
);

const Payout = mongoose.model('Payout', PayoutSchema);
export default Payout;
