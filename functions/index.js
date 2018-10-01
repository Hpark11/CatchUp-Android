
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.firestore.document('messages/{messageId}').onCreate((snap, context) => {
  console.log("snap: ", snap);

  const params = snap.data();
  const title = params.title;
  const pushTokens = params.pushTokens;
  const message = params.message;
  const messageId = snap.id;
  
  return Promise.all(pushTokens.map((token) => {
    console.log("token: ", token);

    const payload = {
      data: {
        data_type: "direct_message",
        title: title,
        message: message,
        message_id: messageId,
      }
    };

    return admin.messaging().sendToDevice(token, payload)
        .then(function(response) {
          console.log("Successfully sent message:", response);
          })
          .catch(function(error) {
          console.log("Error sending message:", error);
    });
  }));
});