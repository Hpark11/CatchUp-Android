package blackburn.io.catchup.ui.detail

import blackburn.io.catchup.app.BaseViewModel
import blackburn.io.catchup.model.Contact
import blackburn.io.catchup.service.app.DataService
import blackburn.io.catchup.service.app.SchedulerUtil
import io.reactivex.Flowable
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject
import javax.inject.Named

class PromiseDetailMembersViewModel @Inject constructor(
  private val scheduler: SchedulerUtil,
  private val data: DataService,
  @Named("realmConfigCatchUp") private val realmConfig: RealmConfiguration
) : BaseViewModel() {
  private val realm: Realm by lazy { Realm.getInstance(realmConfig) }

  fun loadSingleContact(phone: String): Flowable<Contact>? {
    return Contact.find(phone, realm)?.asFlowable()
  }
}