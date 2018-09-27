package blackburn.io.catchup.ui.detail

import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import javax.inject.Inject

class PromiseDetailViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService
): BaseViewModel() {

}