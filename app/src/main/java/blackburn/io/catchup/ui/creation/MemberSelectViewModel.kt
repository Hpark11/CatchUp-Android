package blackburn.io.catchup.ui.creation

import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.ContactService
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class MemberSelectViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  private val contact: ContactService
): BaseViewModel() {

  fun loadContactList() {
    compositeDisposable += contact.requestContactList().switchMap {
      return@switchMap data.requestContacts(it.map { it.phone })
    }.compose(scheduler.forObservable()).subscribe {

    }
//      .map {
////        it.
//      }
  }
}