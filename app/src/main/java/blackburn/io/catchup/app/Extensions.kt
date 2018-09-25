package blackburn.io.catchup.app

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

operator fun AutoClearDisposable.plusAssign(disposable: Disposable) = this.add(disposable)
