package blackburn.io.catchup.service.app

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import blackburn.io.catchup.model.Contact
import blackburn.io.catchup.di.scope.AppScope
import io.reactivex.Observable
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import javax.inject.Inject
import javax.inject.Named

@AppScope
class ContactService @Inject constructor(
  @Named("applicationContext") private val appContext: Context,
  @Named("realmConfigCatchUp") private val realmConfig: RealmConfiguration
) {

  fun requestContactList(): Observable<RealmResults<Contact>> {
    return Observable.create { emitter ->
      val realm = Realm.getInstance(realmConfig)
      if (!emitter.isDisposed) emitter.onNext(
        realm.where(Contact::class.java).findAll()
      )

      val cursor = appContext.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
      )

      val cursorSim = appContext.contentResolver.query(
        Uri.parse("content://icc/adn"),
        null,
        null,
        null,
        null
      )

      try {
        val contactIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneNumberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)
        cursor.moveToFirst()

        do {
          val id = cursor.getString(contactIdIdx)
          val nickname = cursor.getString(nameIdx)
          val phone = eraseKoreaCode(cursor.getString(phoneNumberIdx))

          realm.executeTransactionAsync { strongRealm ->
            strongRealm.insertOrUpdate(
              Contact(phone, nickname, "", "", "")
            )
          }
        } while (cursor.moveToNext())


        var simPhoneName: String? = null
        var simPhoneNo: String? = null

        val contactSimIdIdx = cursorSim.getColumnIndex("name")
        val contactNameIdx = cursorSim.getColumnIndex("number")

        while (cursorSim.moveToNext()) {
          simPhoneName = cursorSim.getString(contactSimIdIdx)
          simPhoneName = simPhoneName?.replace("|", "")

          simPhoneNo = cursorSim.getString(contactNameIdx)
          simPhoneNo = simPhoneNo?.replace("\\D".toRegex(), "")
          simPhoneNo = simPhoneNo?.replace("&".toRegex(), "")

          val phone = eraseKoreaCode(simPhoneNo ?: "")
          val nickname = simPhoneName ?: ""

          realm.executeTransactionAsync { strongRealm ->
            strongRealm.insertOrUpdate(
              Contact(phone, nickname, "", "", "")
            )
          }
        }

      } catch (e: Exception) {
        if (!emitter.isDisposed) emitter.onError(e)

      } finally {
        if (!emitter.isDisposed) emitter.onComplete()
        cursor?.close()
        realm?.close()
      }
    }
  }

  private fun eraseKoreaCode(phoneNumber: String): String {
    var phone = phoneNumber.replace("[^0-9]".toRegex(), "")
    if (phone.startsWith("82")) {
      phone = phone.removePrefix("82")
      phone = "0$phone"
    }
    return phone
  }
}