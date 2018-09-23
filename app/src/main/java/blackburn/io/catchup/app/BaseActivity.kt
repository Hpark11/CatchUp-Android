package blackburn.io.catchup.app

import android.os.Bundle
import android.view.WindowManager
import dagger.android.support.DaggerAppCompatActivity

open class BaseActivity: DaggerAppCompatActivity() {
  protected val disposable: AutoClearDisposable by lazy {
    return@lazy AutoClearDisposable(this)
  }

  protected fun setStatusBarTranslucent(isTranslucent: Boolean) {
    if (isTranslucent) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    } else {
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStatusBarTranslucent(true)
  }
}