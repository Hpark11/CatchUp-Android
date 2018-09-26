package blackburn.io.catchup.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Contact(
  @PrimaryKey var phone: String = "",
  var nickname: String = "",
  var profileImagePath: String = "",
  var email: String = "",
  var pushToken: String = ""
): RealmObject()