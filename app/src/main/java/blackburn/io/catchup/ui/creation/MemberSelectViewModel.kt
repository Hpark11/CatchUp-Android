package blackburn.io.catchup.ui.creation

import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import javax.inject.Inject

class MemberSelectViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService
): BaseViewModel() {

}