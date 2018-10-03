package blackburn.io.catchup.app

import android.arch.lifecycle.LifecycleOwner
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import blackburn.io.catchup.R
import blackburn.io.catchup.app.util.AutoClearDisposable
import blackburn.io.catchup.app.util.StatusBarState
import blackburn.io.catchup.app.util.setStatusBarContentColor

open class BaseWithoutDIActivity: AppCompatActivity() {
  protected val disposable: AutoClearDisposable<LifecycleOwner> by lazy {
    return@lazy AutoClearDisposable<LifecycleOwner>(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = resources.getColor(R.color.md_white_1000)
    setStatusBarContentColor(window, StatusBarState.Light)
  }
}