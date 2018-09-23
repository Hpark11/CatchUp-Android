package blackburn.io.catchup.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
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
        .setPermissions(
          Manifest.permission.READ_CONTACTS,
          Manifest.permission.READ_PHONE_STATE,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION
        ).request()
    }.subscribe({ result ->
      if (result.isGranted) {
//        finishAfterTransition()
      } else {
        Toast.makeText(this,
          "Permission Denied\n" + result.deniedPermissions.toString(), Toast.LENGTH_SHORT)
          .show()
      }
    }, { throwable ->
      throwable.printStackTrace()
    })
  }
}
