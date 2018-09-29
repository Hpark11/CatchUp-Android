package blackburn.io.catchup.ui

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import com.amazonaws.amplify.generated.graphql.GetCatchUpContactQuery
import com.amazonaws.amplify.generated.graphql.ListCatchUpPromisesByContactQuery
import com.apollographql.apollo.api.Response
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  private val pref: SharedPrefService
) : BaseViewModel() {

  val promiseList = MutableLiveData<List<ListCatchUpPromisesByContactQuery.ListCatchUpPromisesByContact>>()

  fun loadPromiseList() {
    compositeDisposable += data.requestPromises(pref.phone).compose(scheduler.forObservable())
      .subscribeBy(
        onNext = {
          promiseList.value = it.data()?.listCatchUpPromisesByContact()
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