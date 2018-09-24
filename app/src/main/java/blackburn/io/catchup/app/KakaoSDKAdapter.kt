package blackburn.io.catchup.app

import android.content.Context
import blackburn.io.catchup.app.App
import com.kakao.auth.*
import javax.inject.Inject
import javax.inject.Named

class KakaoSDKAdapter @Inject constructor(
  @Named("applicationContext") private val context: Context
): KakaoAdapter() {
  override fun getSessionConfig(): ISessionConfig {
    return object : ISessionConfig {

      override fun isSecureMode(): Boolean {
        return false
      }

      override fun getAuthTypes(): Array<AuthType> {
        return arrayOf(AuthType.KAKAO_LOGIN_ALL)
      }

      override fun isUsingWebviewTimer(): Boolean {
        return false
      }

      override fun getApprovalType(): ApprovalType {
        return ApprovalType.INDIVIDUAL
      }

      override fun isSaveFormData(): Boolean {
        return true
      }
    }
  }

  override fun getApplicationConfig(): IApplicationConfig {
    return IApplicationConfig { context }
  }
}