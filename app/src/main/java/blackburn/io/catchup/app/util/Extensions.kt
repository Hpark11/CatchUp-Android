package blackburn.io.catchup.app.util

import android.arch.lifecycle.LifecycleOwner
import blackburn.io.catchup.app.util.AutoClearDisposable
import io.reactivex.disposables.Disposable
import io.realm.RealmModel
import io.realm.RealmResults

operator fun AutoClearDisposable<LifecycleOwner>.plusAssign(disposable: Disposable) = this.add(disposable)

fun <T: RealmModel> RealmResults<T>.asLiveData() = LocalLiveData(this)