package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.model.Contact
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import com.amazonaws.amplify.generated.graphql.CreateCatchUpPromiseMutation
import com.amazonaws.amplify.generated.graphql.UpdateCatchUpPromiseMutation
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

  fun makePromise(): Maybe<CreateCatchUpPromiseMutation.Data> {
    val name = name.value ?: "None"
    val place = placeInfo.value ?: PlaceInfo("None", 0.0, 0.0)

    return Maybe.create { emitter ->
      compositeDisposable += data.requestUser(pref.userId).doOnNext { userQuery ->
        val user = userQuery?.data()?.catchUpUser
        if (user != null) {
          val credit = user.credit() ?: 0
          if (credit <= 0) {
            throw CreditRunoutException("No Credit Left")
          }
        } else {
          throw DataService.QueryException("Invalid User")
        }
      }.compose(scheduler.forObservable()).switchMap {
        return@switchMap data.useCredit(it.data()?.catchUpUser?.id() ?: "")
      }.compose(scheduler.forObservable()).switchMap { response ->
        val phone = response.data()?.useCredit()?.phone()
        val members = mutableListOf<String>()

        if (phone == null) {
          throw PhoneNotFoundException("Invalid PhoneNumber")
        } else {
          members.add(phone)
          members.addAll(0, contacts.value ?: listOf())

          return@switchMap data.createPromise(
            phone,
            name,
            DateUtils.formatISO8601Date(dateTime.value),
            place.address,
            place.latitude,
            place.longitude,
            members.map { "\"${it}\"" }
          )
        }
      }.subscribeBy(
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

  fun editPromise(id: String): Maybe<UpdateCatchUpPromiseMutation.Data> {
    val name = name.value ?: "None"
    val place = placeInfo.value ?: PlaceInfo("None", 0.0, 0.0)
    val contacts = contacts.value ?: listOf()

    return Maybe.create { emitter ->
      compositeDisposable += data.updatePromise(
        id,
        pref.phone,
        name,
        DateUtils.formatISO8601Date(dateTime.value),
        place.address,
        place.latitude,
        place.longitude,
        contacts
      ).subscribeOn(Schedulers.io()).subscribeBy(
        onNext = { response ->
          if (!emitter.isDisposed) {
            response.data()?.let { data ->
              emitter.onSuccess(data)
            }.let {
              if (!emitter.isDisposed) emitter.onError(Throwable("Invalid Data"))
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

  data class PlaceInfo(val address: String, val latitude: Double, val longitude: Double)

  class CreditRunoutException(message: String) : Exception(message)
  class PhoneNotFoundException(message: String) : Exception(message)
}