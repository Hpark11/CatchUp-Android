package blackburn.io.catchup.service.android

import blackburn.io.catchup.service.app.DataService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject

class PushTokenService: FirebaseMessagingService() {

  @Inject
  lateinit var dataService: DataService

  override fun onCreate() {
    super.onCreate()
    val a = 2
  }

  override fun onNewToken(token: String?) {
    super.onNewToken(token)
    dataService.requestAppVersion()
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage?) {
    super.onMessageReceived(remoteMessage)
  }
}