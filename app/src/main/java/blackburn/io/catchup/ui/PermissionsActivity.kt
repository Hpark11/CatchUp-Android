package blackburn.io.catchup.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseWithoutDIActivity

class PermissionsActivity: BaseWithoutDIActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_permissions)
  }
}
