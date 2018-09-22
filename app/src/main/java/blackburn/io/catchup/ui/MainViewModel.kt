package blackburn.io.catchup.ui

import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.service.SchedulerUtil
import javax.inject.Inject

class MainViewModel @Inject constructor(
  private val scheduler: SchedulerUtil
): BaseViewModel() {

}