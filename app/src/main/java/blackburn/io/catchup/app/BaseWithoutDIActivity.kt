package blackburn.io.catchup.app

import android.support.v7.app.AppCompatActivity

open class BaseWithoutDIActivity: AppCompatActivity() {
  protected val disposable: AutoClearDisposable by lazy {
    return@lazy AutoClearDisposable(this)
  }
}