package blackburn.io.catchup.app

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.app.AppCompatActivity
import blackburn.io.catchup.app.util.AutoClearDisposable

open class BaseWithoutDIActivity: AppCompatActivity() {
  protected val disposable: AutoClearDisposable<LifecycleOwner> by lazy {
    return@lazy AutoClearDisposable<LifecycleOwner>(this)
  }
}