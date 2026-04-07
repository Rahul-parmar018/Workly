import mongoose from 'mongoose';

const PayoutSchema = new mongoose.Schema(
  {
    booking: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Booking',
      required: true,
    },
    payment: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Payment',
      required: true,
    },
    provider: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Provider',
      required: true,
    },
    transferId: {
      type: String, // Razorpay Transfer ID
      required: true,
      unique: true,
    },
    amount: {
      type: Number,
      required: true,
    },
    status: {
      type: String,
      enum: ['ON_HOLD', 'RELEASED', 'REVERSED', 'FAILED'],
      default: 'ON_HOLD',
    },
    onHoldUntil: Date,
    releaseAt: Date, // Window expiry time
    releasedAt: Date, // Real release time
    failureReason: String,
    retryCount: {
      type: Number,
      default: 0,
    },
  },
  { timestamps: true }
);

const Payout = mongoose.model('Payout', PayoutSchema);
export default Payout;
