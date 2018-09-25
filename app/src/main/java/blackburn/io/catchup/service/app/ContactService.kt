package blackburn.io.catchup.service.app

import android.content.Context
import android.provider.ContactsContract
import blackburn.io.catchup.data.Contact
import blackburn.io.catchup.di.scope.AppScope
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Named

@AppScope
class ContactService @Inject constructor(
  @Named("applicationContext") private val appContext: Context
) {

  fun requestContactList(): Observable<List<Contact>> {
    return Observable.create { emitter ->
      val contactList = mutableListOf<Contact>()
      val cursor = appContext.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
      )

      cancelOnDisposed(emitter, cursor)

      try {
        val contactIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val phoneNumberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIdIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID)
        cursor.moveToFirst()

        do {
          val id = cursor.getString(contactIdIdx)
          val nickname = cursor.getString(nameIdx)
          val phone = cursor.getString(phoneNumberIdx).replace("[^0-9]".toRegex(), "")

          contactList.add(Contact(phone, nickname, null))
        } while (cursor.moveToNext())

        if (!emitter.isDisposed) emitter.onNext(contactList)
      } catch (e: Exception) {
        if (!emitter.isDisposed) emitter.onError(e)
      } finally {
        if (!emitter.isDisposed) emitter.onComplete()
        cursor?.close()
      }
    }
  }

  private fun <T> cancelOnDisposed(emitter: ObservableEmitter<T>, closeable: Closeable?) {
    emitter.setDisposable(disposable(closeable))
  }

  private fun disposable(closeable: Closeable?): Disposable {
    return object : Disposable {
      override fun dispose() {
        closeable?.close()
      }

      override fun isDisposed() = closeable == null
    }
  }
}