const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotification = functions.firestore
  .document('orders/{orderId}')
  .onUpdate(async (change, context) => {
      
    const newData = change.after.data();
    const oldData = change.before.data();

    // Only proceed if the status actually changed
    if (newData.status === oldData.status) return null;

    // We assume that the user who needs the notification is the one who booked the service
    // However, if the status is "pending" (i.e. a new booking created), both user and provider might be involved.
    // For this flow: notify the USER when Provider changes status (e.g. accepted, arriving, started, completed)
    const userDoc = await admin.firestore()
      .collection('users')
      .doc(newData.userId)
      .get();

    if (!userDoc.exists) {
        console.log("User not found");
        return null;
    }

    const token = userDoc.data().fcmToken;
    if (!token) {
        console.log("No FCM token for user");
        return null; // User hasn't registered a token
    }

    // Determine the message based on the status
    let title = "Order Update";
    let messageBody = `Your order status changed to ${newData.status}`;
    
    switch(newData.status) {
        case "accepted":
            title = "Service Accepted ✅";
            messageBody = `${newData.providerName} has accepted your request.`;
            break;
        case "arriving":
            title = "Provider on the way 🚗";
            messageBody = `${newData.providerName} is arriving at your location.`;
            break;
        case "started":
            title = "Service Started 🛠️";
            messageBody = "Work is now in progress.";
            break;
        case "completed":
            title = "Service Completed 🎉";
            messageBody = "Your service has been completed successfully!";
            break;
    }

    const payload = {
      notification: {
        title: title,
        body: messageBody,
        sound: "default"
      }
    };

    console.log(`Sending notification to ${token} for status ${newData.status}`);
    return admin.messaging().sendToDevice(token, payload);
  });
