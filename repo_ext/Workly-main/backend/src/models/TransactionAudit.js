import mongoose from 'mongoose';

const TransactionAuditSchema = new mongoose.Schema(
  {
    entityType: {
      type: String,
      enum: ['Booking', 'Payment', 'Payout'],
      required: true,
    },
    entityId: {
      type: mongoose.Schema.Types.ObjectId,
      required: true,
    },
    fromStatus: String,
    toStatus: {
      type: String,
      required: true,
    },
    action: String, // e.g., 'WEBHOOK_RECEIVED', 'MANUAL_UPDATE', 'CRON_JOB'
    performedBy: String, // 'SYSTEM', 'ADMIN', 'PROVIDER'
    metadata: Object,
  },
  { timestamps: true }
);

const TransactionAudit = mongoose.model(
  'TransactionAudit',
  TransactionAuditSchema
);
export default TransactionAudit;
