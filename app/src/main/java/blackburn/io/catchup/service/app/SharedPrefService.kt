package blackburn.io.catchup.service.app

import android.content.Context
import blackburn.io.catchup.di.scope.AppScope
import javax.inject.Inject
import javax.inject.Named

@AppScope
class SharedPrefService @Inject constructor(
  @Named("applicationContext") private val context: Context
) {
  private val sharedInfo = context.getSharedPreferences("shared", Context.MODE_PRIVATE)

  var closestTime: Long
    get() = sharedInfo.getLong("scheduledTime", 0L)
    set(value) = sharedInfo.edit().putLong("scheduledTime", value).apply()

  var phone: String
    get() = sharedInfo.getString("phone", "")
    set(value) = sharedInfo.edit().putString("phone", value).apply()

  var pushToken: String
    get() = sharedInfo.getString("pushToken", "")
    set(value) = sharedInfo.edit().putString("pushToken", value).apply()

  var userId: String
    get() = sharedInfo.getString("userId", "")
    set(value) = sharedInfo.edit().putString("userId", value).apply()
}