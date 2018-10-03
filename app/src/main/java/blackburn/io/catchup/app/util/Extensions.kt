package blackburn.io.catchup.app.util

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import blackburn.io.catchup.app.util.AutoClearDisposable
import io.reactivex.disposables.Disposable
import io.realm.RealmModel
import io.realm.RealmResults

operator fun AutoClearDisposable<LifecycleOwner>.plusAssign(disposable: Disposable) = this.add(disposable)

fun <T: RealmModel> RealmResults<T>.asLiveData() = LocalLiveData(this)

fun dpFromPx(context: Context, px: Float): Float {
  return px / context.resources.displayMetrics.density
}

fun pxFromDp(context: Context, dp: Float): Float {
  return dp * context.resources.displayMetrics.density
}

enum class StatusBarState {
  Light,
  Dark
}

fun setStatusBarContentColor(window: Window, statusBarState: StatusBarState) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    var newUiVisibility = window.decorView.systemUiVisibility

    if (statusBarState === StatusBarState.Light) {
      //Dark Text to show up on your light status bar
      newUiVisibility = newUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else if (statusBarState === StatusBarState.Dark) {
      //Light Text to show up on your dark status bar
      newUiVisibility = newUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }

    window.decorView.systemUiVisibility = newUiVisibility
  }
}