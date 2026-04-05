import 'dotenv/config';

import app from './app.js';
import connectDB from './config/db.js';
import initSchedulers from './scheduler.js';

// Connect to Database
connectDB();

// Initialize Background Chronic Jobs (Payouts, Reconciliation)
initSchedulers();

const PORT = process.env.PORT || 5000;

const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running in ${process.env.NODE_ENV} mode on port ${PORT}`);
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (err, promise) => {
  console.error(`Error: ${err.message}`);
  // Close server & exit process
  server.close(() => process.exit(1));
});
