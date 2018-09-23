package blackburn.io.catchup.ui

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.app.Define
import javax.inject.Inject

class EntranceActivity: BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: EntranceViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)

    viewModel = ViewModelProviders.of(this, viewModelFactory).get(EntranceViewModel::class.java)
    bindViewModel()

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

        }
      }
    })


  }

  private val isNecessaryPermissionsGranted
    get() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
