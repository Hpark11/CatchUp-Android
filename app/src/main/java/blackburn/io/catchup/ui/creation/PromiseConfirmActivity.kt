package blackburn.io.catchup.ui.creation

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.WindowManager
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.app.BaseWithoutDIActivity
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.util.StatusBarState
import blackburn.io.catchup.app.util.setStatusBarContentColor
import com.afollestad.materialdialogs.MaterialDialog
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.*
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import kotlinx.android.synthetic.main.activity_promise_confirm.*

class PromiseConfirmActivity: BaseWithoutDIActivity() {

  private var name: String = ""
  private var dateTime: String = ""
  private var location: String = ""
  private var isEdit: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_promise_confirm)

    window.statusBarColor = resources.getColor(R.color.dark_sky_blue)
    setStatusBarContentColor(window, StatusBarState.Dark)
    window.enterTransition = TransitionInflater.from(this).inflateTransition(R.transition.fade)

    name = intent.getStringExtra("name")
    dateTime = intent.getStringExtra("dateTime")
    location = intent.getStringExtra("location")
    isEdit = intent.getBooleanExtra("isEdit", false)
    val id = intent.getStringExtra("id")

    promiseConfirmTitleTextView.text = resources.getString(
      if (isEdit) R.string.promise_confirm_title_edit else R.string.promise_confirm_title_add
    )

    promiseDateTimeTextView.text = dateTime
    promiseLocationTextView.text = location
    val appUrl = "${Define.APP_MARKET_URL}$packageName"

    promiseConfirmButton.setOnClickListener {
      MaterialDialog.Builder(this@PromiseConfirmActivity)
        .title(R.string.dialog_title_notify_promise)
        .content(R.string.dialog_content_notify_promise)
        .onPositive { dialog, which ->

          val params = LocationTemplate.newBuilder(location,
            ContentObject.newBuilder(name,
              "http://www.kakaocorp.com/images/logo/og_daumkakao_151001.png",
              LinkObject.newBuilder()
                .setWebUrl(appUrl)
                .setMobileWebUrl(appUrl)
                .setAndroidExecutionParams("param1=$id&param2=$name")
                .setIosExecutionParams("param1=$id&param2=$name")
                .build())
              .setDescrption("일시: $dateTime\n장소: $location")
              .build())
            .addButton(
              ButtonObject("앱에서 보기", LinkObject.newBuilder()
                .setWebUrl(appUrl)
                .setMobileWebUrl(appUrl)
                .setAndroidExecutionParams("param1=$id&param2=$name")
                .setIosExecutionParams("param1=$id$&param2=$name")
                .build()))
            .setAddressTitle(name)
            .build()

          val serverCallbackArgs = HashMap<String, String>()
          serverCallbackArgs["user_id"] = "\${current_user_id}"
          serverCallbackArgs["product_id"] = "\${shared_product_id}"

          KakaoLinkService.getInstance().sendDefault(
            this@PromiseConfirmActivity, params,
            object : ResponseCallback<KakaoLinkResponse>() {
              override fun onFailure(errorResult: ErrorResult) {
                Log.e(this@PromiseConfirmActivity.javaClass.simpleName, errorResult.errorMessage)
              }

              override fun onSuccess(result: KakaoLinkResponse) {
              }
            })

          finishAfterTransition()
        }
        .onNegative { dialog, which ->
          finishAfterTransition()
        }
        .positiveText(R.string.confirm)
        .negativeText(R.string.cancel)
        .show()
    }
  }
}
