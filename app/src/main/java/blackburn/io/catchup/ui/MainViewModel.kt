package blackburn.io.catchup.ui

import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.app.SchedulerUtil
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val scheduler: SchedulerUtil
): BaseViewModel() {

}