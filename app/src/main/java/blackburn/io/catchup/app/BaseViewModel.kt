package blackburn.io.catchup.app

import android.arch.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class BaseViewModel: ViewModel() {
  protected var compositeDisposable: CompositeDisposable = CompositeDisposable()

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.dispose()
    compositeDisposable.clear()
  }
}