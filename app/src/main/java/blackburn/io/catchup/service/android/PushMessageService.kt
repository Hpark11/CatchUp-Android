package blackburn.io.catchup.service.android

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import blackburn.io.catchup.R
import blackburn.io.catchup.ui.MainActivity
import java.util.*

class PushMessageService: FirebaseMessagingService() {

  override fun onNewToken(token: String?) {
    super.onNewToken(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage?) {
    super.onMessageReceived(remoteMessage)
    remoteMessage?.let { remoteMessage ->
      val dataType = remoteMessage.data[getString(R.string.data_type)]
      if (dataType == getString(R.string.direct_message)) {
        val title = remoteMessage.data[getString(R.string.data_title)] ?: ""
        val message = remoteMessage.data[getString(R.string.data_message)] ?: ""
        val messageId = remoteMessage.data[getString(R.string.data_message_id)] ?: ""
        sendMessageNotification(title, message, messageId)
      }
    }
  }

  private fun sendMessageNotification(title: String, message: String, messageId: String) {
    val notificationId = buildNotificationId(messageId)

    // Instantiate a Builder object.
    val builder = NotificationCompat.Builder(
      this,
      getString(R.string.default_notification_channel_id)
    )

    // Creates an Intent for the Activity
    val pendingIntent = Intent(this, MainActivity::class.java)

    // Sets the Activity to start in a new, empty task
    pendingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

    // Creates the PendingIntent
    val notifyPendingIntent = PendingIntent.getActivity(
      this,
      0,
      pendingIntent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    builder.setSmallIcon(R.drawable.icon_statusbar)
      .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.mipmap.ic_launcher))
      .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
      .setContentTitle(title)
      .setColor(resources.getColor(R.color.md_blue_600))
      .setAutoCancel(true)
      .setSubText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setOnlyAlertOnce(true)

    builder.setContentIntent(notifyPendingIntent)
    val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    mNotificationManager.notify(notificationId, builder.build())
  }

  private fun buildNotificationId(id: String): Int {
    Log.d("PUSHMESSAGE", "buildNotificationId: building a notification id.")

    var notificationId = 0
    for (i in 0..8) {
      notificationId += id[0].toInt()
    }
    Log.d("PUSHMESSAGE", "buildNotificationId: id: $id")
    Log.d("PUSHMESSAGE", "buildNotificationId: notification id:$notificationId")
    return notificationId
  }
}