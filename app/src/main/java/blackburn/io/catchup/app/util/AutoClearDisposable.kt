package blackburn.io.catchup.app.util

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class AutoClearDisposable<L: LifecycleOwner>(
  private val lifecycleOwner: L,
  private val clearOnStop: Boolean = true,
  private val compositeDisposable: CompositeDisposable = CompositeDisposable()
): LifecycleObserver {

  fun add(disposable: Disposable) {
    check(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))
    compositeDisposable.add(disposable)
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun clear() {
    if (!clearOnStop) { return }
    compositeDisposable.clear()
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun detach() {
    compositeDisposable.clear()
    lifecycleOwner.lifecycle.removeObserver(this)
  }
}