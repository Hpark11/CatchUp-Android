package blackburn.io.catchup.ui.detail

import android.arch.lifecycle.MutableLiveData
import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.ui.creation.NewPromiseViewModel
import com.amazonaws.amplify.generated.graphql.GetCatchUpPromiseQuery
import com.amazonaws.util.DateUtils
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class PromiseDetailViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService
): BaseViewModel() {

  val promiseDetail: MutableLiveData<GetCatchUpPromiseQuery.GetCatchUpPromise> = MutableLiveData()

  fun loadPromise(id: String) {
    compositeDisposable += data.requestPromise(id).compose(scheduler.forObservable()).subscribeBy(
      onNext = { response ->
        response.data()?.catchUpPromise?.let { promise ->
          promiseDetail.value = promise
        }
      },
      onError = {
        it.printStackTrace()
      }
    )
  }
}