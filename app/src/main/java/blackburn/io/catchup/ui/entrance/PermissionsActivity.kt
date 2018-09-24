package blackburn.io.catchup.ui.entrance

import android.Manifest
import android.os.Bundle
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseWithoutDIActivity
import blackburn.io.catchup.app.plusAssign
import com.jakewharton.rxbinding2.view.RxView
import com.tedpark.tedpermission.rx2.TedRx2Permission
import kotlinx.android.synthetic.main.activity_permissions.*

class PermissionsActivity: BaseWithoutDIActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_permissions)

    disposable += RxView.clicks(permissionApproveButton).switchMapSingle {
      return@switchMapSingle TedRx2Permission.with(this)
        .setDeniedMessage(R.string.permission_deny_message)
        .setPermissions(
          Manifest.permission.READ_CONTACTS,
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ).request()
    }.subscribe({ result ->
      if (result.isGranted) {
        setResult(RESULT_CODE_PERMISSION_GRANTED)
        finish()
      }
    }, { throwable ->
      throwable.printStackTrace()
    })
  }

  companion object {
    val RESULT_CODE_PERMISSION_GRANTED = 9999
  }
}
