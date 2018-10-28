package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.model.Contact
import blackburn.io.catchup.model.PlaceInfo
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import com.amazonaws.amplify.generated.graphql.BatchGetCatchUpContactsQuery
import com.amazonaws.amplify.generated.graphql.CreateCatchUpPromiseMutation
import com.amazonaws.amplify.generated.graphql.UpdateCatchUpPromiseMutation
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.util.DateUtils
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.Exception

class NewPromiseViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  @Named("realmConfigCatchUp") private val realmConfig: RealmConfiguration,
  private val pref: SharedPrefService
) : BaseViewModel() {

  private val realm: Realm by lazy { Realm.getInstance(realmConfig) }

  val name: MutableLiveData<String> = MutableLiveData()
  val placeInfo: MutableLiveData<PlaceInfo> = MutableLiveData()
  val dateTime: MutableLiveData<Date> = MutableLiveData()

  val nameInput: PublishSubject<String> = PublishSubject.create()
  val placeInput: PublishSubject<PlaceInfo> = PublishSubject.create()
  val dateTimeInput: PublishSubject<Date> = PublishSubject.create()

  init {
    compositeDisposable += nameInput.subscribe { name.value = it }
    compositeDisposable += placeInput.subscribe { placeInfo.value = it }
    compositeDisposable += dateTimeInput.subscribe { dateTime.value = it }
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

  fun makePromise(id: String?): Maybe<UpdateCatchUpPromiseMutation.Data> {
    val name = name.value ?: "None"
    val place = placeInfo.value ?: PlaceInfo("None", 0.0, 0.0)

    return Maybe.create { emitter ->
      val phone = pref.phone
      val contacts = listOf(phone)

      if (phone.isEmpty()) {
        throw PhoneNotFoundException("Invalid PhoneNumber")
      } else {

        compositeDisposable += data.updatePromise(
          id ?: UUID.randomUUID().toString(),
          phone,
          name,
          DateUtils.format(DateUtils.ALTERNATE_ISO8601_DATE_PATTERN, dateTime.value),
          place.address,
          place.latitude,
          place.longitude,
          contacts
        ).subscribeBy(
          onNext = { response ->
            if (!emitter.isDisposed) {
              response.data()?.let { data ->
                emitter.onSuccess(data)
              }.let {
                if (!emitter.isDisposed) emitter.onError(DataService.QueryException("No Data"))
              }
            }
          },
          onError = {
            it.printStackTrace()
            if (!emitter.isDisposed) emitter.onError(it)
          }
        )
      }
    }
  }

  fun loadContacts(phones: List<String>): Maybe<List<BatchGetCatchUpContactsQuery.BatchGetCatchUpContact>> {
    return Maybe.create { emitter ->
      compositeDisposable += data.requestContacts(phones).compose(scheduler.forObservable())
        .subscribeBy(
          onNext = { data ->
            data.data()?.batchGetCatchUpContacts()?.let {
              if(!emitter.isDisposed) emitter.onSuccess(it)
            }
          },
          onError = {
            it.printStackTrace()
            if(!emitter.isDisposed) emitter.onError(it)
          }
        )
    }
  }

  class CreditRunoutException(message: String) : Exception(message)
  class PhoneNotFoundException(message: String) : Exception(message)
}