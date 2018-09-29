package blackburn.io.catchup.ui.entrance

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Log
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.ui.MainActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

class EntranceActivity: BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var sessionCallback: SessionCallback
  private lateinit var viewModel: EntranceViewModel

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

    if (!isNecessaryPermissionsGranted) {
      startActivity(Intent(this@EntranceActivity, PermissionsActivity::class.java))
    }
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
      }
    })

    viewModel.initDoneWithPhone.observe(this, Observer {
      it?.let { phone ->
        val intent = Intent(this@EntranceActivity, MainActivity::class.java)
        intent.putExtra("phone", phone)
        startActivity(intent)
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
          if (!isNecessaryPermissionsGranted) return

          val phoneMgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
          val phone = phoneMgr.line1Number?.replace("[^0-9]".toRegex(), "") ?: ""

          FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener {
              viewModel.attachToken(phone, it.token)
            }.addOnFailureListener {
              it.printStackTrace()
            }

          viewModel.updateCatchUpUser(
            "${response.id}",
            phone,
            response.kakaoAccount.email,
            response.nickname,
            response.profileImagePath,
            response.kakaoAccount.gender?.value,
            response.kakaoAccount.birthday,
            response.kakaoAccount.ageRange?.value,
            null
          )
        }
      })
    }

    override fun onSessionOpenFailed(exception: KakaoException?) {
      exception?.printStackTrace()
    }
  }
}
