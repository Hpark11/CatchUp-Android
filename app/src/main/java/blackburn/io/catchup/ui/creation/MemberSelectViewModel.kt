package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.LiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.util.asLiveData
import blackburn.io.catchup.model.Contact
import blackburn.io.catchup.service.app.ContactService
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import io.reactivex.rxkotlin.merge
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import javax.inject.Inject
import javax.inject.Named

class MemberSelectViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  private val contact: ContactService,
  @Named("realmConfigCatchUp") private val realmConfig: RealmConfiguration
) : BaseViewModel() {

  private val realm: Realm by lazy {
    Realm.getInstance(realmConfig)
  }

  fun getContacts(): LiveData<RealmResults<Contact>> {
    return realm.where(Contact::class.java).findAllAsync().asLiveData()
  }

  fun loadContactList() {
    compositeDisposable += contact.requestContactList().switchMap { list ->
      val ops = list.chunked(Define.DYNAMODB_BATCHGET_LIMIT).map { chunk ->
        data.requestContacts(chunk.map { it.phone })
      }

      return@switchMap ops.merge()
    }.compose(scheduler.forObservable()).subscribeBy(
      onNext = {
        it.data()?.batchGetCatchUpContacts()?.let { contacts ->
          realm.executeTransactionAsync { strongRealm ->
            contacts.forEach { singleContact ->
              singleContact?.phone()?.let { phone ->
                var contact = strongRealm.where(Contact::class.java)
                  .equalTo("phone", phone)
                  .findFirst()

                if (contact == null) {
                  contact = strongRealm.createObject(Contact::class.java, phone)
                }

                contact?.profileImagePath = singleContact.profileImagePath() ?: ""
                contact?.pushToken = singleContact.pushToken() ?: ""
              }
            }
          }
        }
      },
      onError = {
        it.printStackTrace()
      }
    )
  }

  override fun onCleared() {
    super.onCleared()
    realm.close()
  }
}