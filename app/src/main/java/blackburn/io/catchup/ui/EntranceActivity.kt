package blackburn.io.catchup.ui

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.util.Log
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.service.app.DataService
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import com.kakao.util.helper.log.Logger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

class EntranceActivity: BaseActivity() {

  @Inject
  lateinit var dataService: DataService

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var sessionCallback: SessionCallback
  private lateinit var viewModel: EntranceViewModel
  private var isSignInDone = false

  private val isNecessaryPermissionsGranted
    get() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[EntranceViewModel::class.java]
    bindViewModel()

    sessionCallback = SessionCallback()
    Session.getCurrentSession().addCallback(sessionCallback)
    Session.getCurrentSession().checkAndImplicitOpen()

    if (dataService == null) {
      val b = 2
    }

    try {
      val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)

      for (signature in info.signatures) {
        val md = MessageDigest.getInstance("SHA")
        md.update(signature.toByteArray())
        Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
      }
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    } catch (e: NoSuchAlgorithmException) {
      e.printStackTrace()
    }

    viewModel.checkAppVersion()
  }

  private fun bindViewModel() {
    viewModel.appVersion.observe(this, Observer {
      it?.let { version ->
        if (version.major > Define.VERSION_MAJOR) {

        }

        if (version.minor > Define.VERSION_MINOR) {

        }

        if (version.revision > Define.VERSION_REVISION) {

        }

        if (!isNecessaryPermissionsGranted) {
          startActivity(Intent(this@EntranceActivity, PermissionsActivity::class.java))
        }
      }
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
      return
    }

    super.onActivityResult(requestCode, resultCode, data)
  }

  override fun onDestroy() {
    super.onDestroy()
    Session.getCurrentSession().removeCallback(sessionCallback)
  }

  private inner class SessionCallback: ISessionCallback {
    override fun onSessionOpened() {

      val keys = mutableListOf<String>()
      keys.add("properties.nickname")
      keys.add("properties.profile_image")
      keys.add("properties.thumbnail_image")
      keys.add("kakao_account.email")
      keys.add("kakao_account.age_range")
      keys.add("kakao_account.birthday")
      keys.add("kakao_account.gender")

      UserManagement.getInstance().me(keys, object : MeV2ResponseCallback() {
        override fun onFailure(errorResult: ErrorResult?) {
          errorResult?.exception?.printStackTrace()
        }

        override fun onSessionClosed(errorResult: ErrorResult) {
          errorResult.exception.printStackTrace()
        }

        override fun onSuccess(response: MeV2Response) {
          Logger.d("user id : " + response.id)
          Logger.d("email: " + response.kakaoAccount.email)
          Logger.d("birthday: " + response.kakaoAccount.birthday)
          Logger.d("gender: " + response.kakaoAccount.gender)
          Logger.d("profile image: " + response.profileImagePath)
          Logger.d("thumbnail image: " + response.thumbnailImagePath)
          Logger.d("Nickname: " + response.nickname)
          Logger.d("ageRange: " + response.kakaoAccount.ageRange)
          Logger.d("displayId: " + response.kakaoAccount.displayId)
          Logger.d("phoneNumber: " + response.kakaoAccount.phoneNumber)

          if (!isNecessaryPermissionsGranted) {
            isSignInDone = true
          } else {
            startActivity(Intent(this@EntranceActivity, MainActivity::class.java))
          }
        }
      })
    }

    override fun onSessionOpenFailed(exception: KakaoException?) {
      exception?.printStackTrace()
    }
  }
}
