package blackburn.io.catchup.ui.creation

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.WindowManager
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseWithoutDIActivity
import blackburn.io.catchup.app.Define
import com.afollestad.materialdialogs.MaterialDialog
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.ButtonObject
import com.kakao.message.template.ContentObject
import com.kakao.message.template.LinkObject
import com.kakao.message.template.LocationTemplate
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import kotlinx.android.synthetic.main.activity_promise_confirm.*

class PromiseConfirmActivity: BaseWithoutDIActivity() {

  private var name: String = ""
  private var dateTime: String = ""
  private var location: String = ""
  private var members: String = ""
  private var isEdit: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_promise_confirm)

    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = resources.getColor(R.color.dark_sky_blue)
    window.enterTransition = TransitionInflater.from(this).inflateTransition(R.transition.fade)

    name = intent.getStringExtra("name")
    dateTime = intent.getStringExtra("dateTime")
    location = intent.getStringExtra("location")
    members = intent.getStringExtra("members")
    isEdit = intent.getBooleanExtra("isEdit", false)
    promiseConfirmTitleTextView.text = resources.getString(
      if (isEdit) R.string.promise_confirm_title_edit else R.string.promise_confirm_title_add
    )

    promiseDateTimeTextView.text = dateTime
    promiseLocationTextView.text = location
    promiseMembersTextView.text = members

    promiseConfirmButton.setOnClickListener {
      MaterialDialog.Builder(this@PromiseConfirmActivity)
        .title(R.string.dialog_title_notify_promise)
        .content(R.string.dialog_content_notify_promise)
        .onPositive { dialog, which ->

          val params = LocationTemplate.newBuilder(location,
            ContentObject.newBuilder("테스트",
              "http://www.kakaocorp.com/images/logo/og_daumkakao_151001.png",
              LinkObject.newBuilder()
                .setWebUrl(Define.APP_MARKET_URL)
                .setMobileWebUrl(Define.APP_MARKET_URL)
                .build())
              .setDescrption(members)
              .build())
            .addButton(
              ButtonObject("앱에서 보기", LinkObject.newBuilder()
                .setWebUrl(Define.APP_MARKET_URL)
                .setMobileWebUrl(Define.APP_MARKET_URL)
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
