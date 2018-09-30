package blackburn.io.catchup.app

import android.arch.lifecycle.LifecycleOwner
import blackburn.io.catchup.app.util.AutoClearDisposable
import dagger.android.support.DaggerFragment

open class BaseFragment: DaggerFragment() {
  protected val disposable: AutoClearDisposable<LifecycleOwner> by lazy {
    return@lazy AutoClearDisposable<LifecycleOwner>(this)
  }
}