package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.model.Contact
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import com.amazonaws.util.DateUtils
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class NewPromiseViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  @Named("realmConfigCatchUp") private val realmConfig: RealmConfiguration
): BaseViewModel() {

  private val realm: Realm by lazy { Realm.getInstance(realmConfig) }

  val name: MutableLiveData<String> = MutableLiveData()
  val placeInfo: MutableLiveData<PlaceInfo> = MutableLiveData()
  val dateTime: MutableLiveData<Date> = MutableLiveData()
  val contacts: MutableLiveData<List<String>> = MutableLiveData()

  val nameInput: PublishSubject<String> = PublishSubject.create()
  val placeInput: PublishSubject<PlaceInfo> = PublishSubject.create()
  val dateTimeInput: PublishSubject<Date> = PublishSubject.create()
  val contactsInput: PublishSubject<List<String>> = PublishSubject.create()

  init {
    compositeDisposable += nameInput.subscribe { name.value = it }
    compositeDisposable += placeInput.subscribe { placeInfo.value = it }
    compositeDisposable += dateTimeInput.subscribe { dateTime.value = it }
    compositeDisposable += contactsInput.subscribe { contacts.value = it }
  }

  fun loadSingleContact(phone: String): Flowable<Contact>? {
    return Contact.find(phone, realm)?.asFlowable()
  }

  fun loadPromise(id: String) {
    compositeDisposable += data.requestPromise(id).compose(scheduler.forObservable()).subscribeBy(
      onNext = { response ->
        response.data()?.catchUpPromise?.let { promise ->
          promise.name()?.let { nameInput.onNext(it) }

          promise.dateTime()?.let { dateTimeInput.onNext(DateUtils.parseISO8601Date(it)) }
          promise.contacts()?.let { contactsInput.onNext(it) }

          val address = promise.address()
          val lat = promise.latitude()
          val lng = promise.longitude()

          if (address == null || lat == null || lng == null) {
            return@let
          } else {
            placeInput.onNext(PlaceInfo(address, lat, lng))
          }
        }
      },
      onError = {
        it.printStackTrace()
      }
    )
  }

  data class PlaceInfo(val address: String, val latitude: Double, val longitude: Double)
}