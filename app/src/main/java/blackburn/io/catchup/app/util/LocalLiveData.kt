package blackburn.io.catchup.app.util

import android.arch.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults

class LocalLiveData<T : RealmModel>(
  private val realmResults: RealmResults<T>
): LiveData<RealmResults<T>>() {

  private val changeListener = RealmChangeListener<RealmResults<T>> {
    results -> value = results
  }

  override fun onActive() {
    realmResults.addChangeListener(changeListener)
  }

  override fun onInactive() {
    realmResults.removeChangeListener(changeListener)
  }
}