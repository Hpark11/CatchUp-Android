package blackburn.io.catchup.app.util

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class AutoClearDisposable(
  private val lifecycleOwner: AppCompatActivity,
  private val clearOnStop: Boolean = true,
  private val compositeDisposable: CompositeDisposable = CompositeDisposable()
): LifecycleObserver {

  fun add(disposable: Disposable) {
    check(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))
    compositeDisposable.add(disposable)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun clear() {
    if (!clearOnStop && !lifecycleOwner.isFinishing) {
      return
    }
    compositeDisposable.clear()
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun detach() {
    compositeDisposable.clear()
    lifecycleOwner.lifecycle.removeObserver(this)
  }
}