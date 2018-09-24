package blackburn.io.catchup.service.android

import blackburn.io.catchup.service.app.DataService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class PushTokenService: FirebaseMessagingService() {

  override fun onNewToken(token: String?) {
    super.onNewToken(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage?) {
    super.onMessageReceived(remoteMessage)
  }
}