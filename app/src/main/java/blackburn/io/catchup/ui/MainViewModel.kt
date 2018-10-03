package blackburn.io.catchup.ui

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.model.AppVersion
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import blackburn.io.catchup.ui.entrance.EntranceViewModel
import com.amazonaws.amplify.generated.graphql.GetCatchUpContactQuery
import com.amazonaws.amplify.generated.graphql.ListCatchUpPromisesByContactQuery
import com.amazonaws.util.DateUtils
import com.apollographql.apollo.api.Response
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  private val pref: SharedPrefService
) : BaseViewModel() {

  private var promiseList = listOf<ListCatchUpPromisesByContactQuery.ListCatchUpPromisesByContact>()
  val filteredList = MutableLiveData<List<ListCatchUpPromisesByContactQuery.ListCatchUpPromisesByContact>>()
  val filterInput: PublishSubject<Pair<Int, Int>> = PublishSubject.create()
  val appVersion: MutableLiveData<AppVersion> = MutableLiveData()

  init {
    compositeDisposable += filterInput.compose(scheduler.forObservable()).subscribeBy(
      onNext = { pair ->
        val calendar = Calendar.getInstance()
        calendar.set(pair.first, pair.second - 1, 1, 0, 0)
        val start = calendar.time
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.time

        filteredList.value = promiseList.filter { promise ->
          val timestamp = DateUtils.parseISO8601Date(promise.dateTime())
      timestamp.time in start.time until end.time
        }
      },
      onError = {
        it.printStackTrace()
      }
    )
  }

  fun checkAppVersion() {
    compositeDisposable += data.requestAppVersion().compose(scheduler.forObservable()).subscribeBy(
      onNext = { response ->
        response.data()?.checkAppVersion()?.let {
          appVersion.value = AppVersion(
            it.major() ?: Define.VERSION_MAJOR,
            it.minor() ?: Define.VERSION_MINOR,
            it.revision() ?: Define.VERSION_REVISION
          )
        }
      },
      onError = {
        it.printStackTrace()
      })
  }

  fun loadPromiseList(year: Int, month: Int) {
    compositeDisposable += data.requestPromises(pref.phone).compose(scheduler.forObservable())
      .subscribeBy(
        onNext = {
          it.data()?.listCatchUpPromisesByContact()?.let { list ->
            promiseList = list
            filterInput.onNext(Pair(year, month))
          }
        },
        onError = {
          it.printStackTrace()
        }
      )
  }

  fun loadContact(phone: String): Observable<Response<GetCatchUpContactQuery.Data>> {
    return data.requestContact(phone).compose(scheduler.forObservable())
  }
}