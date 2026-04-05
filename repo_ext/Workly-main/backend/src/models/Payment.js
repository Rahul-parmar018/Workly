import mongoose from 'mongoose';

const PaymentSchema = new mongoose.Schema(
  {
    booking: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Booking',
      required: true,
    },
    razorpayOrderId: {
      type: String,
      required: true,
      unique: true,
    },
    razorpayPaymentId: {
      type: String,
      unique: true,
      sparse: true,
    },
    amount: {
      type: Number,
      required: true,
    },
    currency: {
      type: String,
      default: 'INR',
    },
    commissionAmount: Number,
    providerPayoutAmount: Number,
    status: {
      type: String,
      enum: ['CREATED', 'CAPTURED', 'FAILED', 'REFUNDED'],
      default: 'CREATED',
    },
    refunds: [
      {
        refundId: String,
        amount: Number,
        status: String,
        createdAt: Date,
      },
    ],
  },
  { timestamps: true }
);

const Payment = mongoose.model('Payment', PaymentSchema);
export default Payment;
