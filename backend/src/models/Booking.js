import mongoose from 'mongoose';

const BookingSchema = new mongoose.Schema(
  {
    user: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User',
      required: true,
    },
    provider: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Provider',
      required: true,
    },
    service: {
      name: String,
      category: String,
      price: Number,
    },
    status: {
      type: String,
      enum: [
        'PENDING_PAYMENT',
        'CONFIRMED',
        'IN_PROGRESS',
        'COMPLETED',
        'DISPUTED',
        'CANCELLED',
        'PAYMENT_FAILED',
        'REFUND_PENDING',
      ],
      default: 'PENDING_PAYMENT',
    },
    scheduledAt: Date,
    completedAt: Date,
    metadata: Object,
  },
  { timestamps: true }
);

const Booking = mongoose.model('Booking', BookingSchema);
export default Booking;
