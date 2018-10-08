package blackburn.io.catchup.service.android

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import blackburn.io.catchup.R
import android.app.NotificationChannel
import android.os.Build
import blackburn.io.catchup.ui.entrance.EntranceActivity

class PushMessageService: FirebaseMessagingService() {

  override fun onNewToken(token: String?) {
    super.onNewToken(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage?) {
    super.onMessageReceived(remoteMessage)
    remoteMessage?.let { remoteMessage ->
      val dataType = remoteMessage.data[getString(R.string.data_type)]
      if (dataType == getString(R.string.catchup_message)) {
        val title = remoteMessage.data[getString(R.string.data_title)] ?: ""
        val message = remoteMessage.data[getString(R.string.data_body)] ?: ""
        val messageId = remoteMessage.data[getString(R.string.data_message_id)] ?: ""
        sendMessageNotification(title, message, messageId)
      }
    }
  }

  private fun sendMessageNotification(title: String, message: String, messageId: String) {
    val pendingIntent = Intent(this, EntranceActivity::class.java)
    pendingIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

    val notifyPendingIntent = PendingIntent.getActivity(
      this,
      0,
      pendingIntent,
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    val channelId = "Default"
    val builder = NotificationCompat.Builder(this, channelId)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(title)
      .setContentText(message)
      .setAutoCancel(true)
      .setContentIntent(notifyPendingIntent)

    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(channelId, getString(R.string.default_notification_channel_id), NotificationManager.IMPORTANCE_DEFAULT)
      manager.createNotificationChannel(channel)
    }
    manager.notify(0, builder.build())
  }
}