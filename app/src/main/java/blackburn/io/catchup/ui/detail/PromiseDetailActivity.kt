package blackburn.io.catchup.ui.detail

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.ui.creation.NewPromiseActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_promise_detail.*
import java.util.*
import javax.inject.Inject
import android.arch.lifecycle.Observer
import com.amazonaws.util.DateUtils
import java.text.SimpleDateFormat

class PromiseDetailActivity : BaseActivity(), HasSupportFragmentInjector {
  companion object {
    val RESULT_CODE_PROMISE_EDIT_OCCURRED = 9942
  }

  enum class DisplayType {
    Map,
    Users
  }

  private var currentDisplayType = DisplayType.Map
  private val promiseDetailMapFragment = PromiseDetailMapFragment()
  private val promiseDetailUsersFragment = PromiseDetailMembersFragment()

  private lateinit var id: String
  private var isEditOccurred = false

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

  private lateinit var viewModel: PromiseDetailViewModel

  override fun supportFragmentInjector(): AndroidInjector<Fragment> {
    return fragmentInjector
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_promise_detail)

    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = resources.getColor(R.color.dark_sky_blue)

    id = intent.getStringExtra("id") ?: ""
    val timestamp = intent.getStringExtra("timestamp") ?: ""

    viewModel = ViewModelProviders.of(this, viewModelFactory)[PromiseDetailViewModel::class.java]
    bindViewModel()

    promiseDetailActionBar.setCenterTextColor(resources.getColor(R.color.md_white_1000))
    promiseDetailToggleButton.setOnClickListener { change() }
    change()

    promiseDetailActionBar.setFirstLeftButtonClickListener(View.OnClickListener {
      finishWithEditCheck()
    })

    promiseEditButton.setOnClickListener {
      val intent = Intent(this@PromiseDetailActivity, NewPromiseActivity::class.java)
      intent.putExtra("id", id)
      startActivityForResult(intent, 100)
    }

    updateCurrentInfo(true)

    promiseDetailRefreshButton.setOnClickListener { view ->
      if (isLocationServiceEnabled()) {
        timestamp.toLongOrNull()?.let {
          val current = Calendar.getInstance().timeInMillis

          if (current >= (it - Define.ACTIVATE_PERIOD) && current <= it) {
            updateCurrentInfo(false)
          } else {
            Toast.makeText(this@PromiseDetailActivity, "약속 활성화 시간 (약속시간 두시간 이내)이 아니여서 친구들의 상태를 확인 할 수 없어요", Toast.LENGTH_LONG).show()
          }
        }

      } else {
        Toast.makeText(this@PromiseDetailActivity, "GPS를 켜지 않아 친구들의 위치정보를 확인할 수 없어요", Toast.LENGTH_LONG).show()
      }
    }
  }

  private fun bindViewModel() {
    viewModel.promiseDetail.observe(this, Observer { promise ->
      promiseDetailActionBar.setCenterText(promise?.name() ?: "")

      promiseDetailDateTimeTextView.text = SimpleDateFormat(
        "MM.dd(EEEE) a hh시 mm분",
        Locale.getDefault()
      ).format(DateUtils.parseISO8601Date(promise?.dateTime()))

//      val timestamp = (response.data()?.promise()?.timestamp() ?: "0").toLong()
//      val calendar = Calendar.getInstance()
//      calendar.timeInMillis = timestamp

//      promiseDetailMapFragment.updateCurrentInfo(response.data()?.promise())
//      promiseDetailUsersFragment.updateCurrentInfo(response.data()?.promise())
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (resultCode) {
      NewPromiseActivity.RESULT_CODE_PROMISE_EDITED -> {
        updateCurrentInfo(true)
        isEditOccurred = true
      }
    }
  }

  private fun updateCurrentInfo(isInit: Boolean) {
//    App.apolloClient.query(
//      GetPromiseQuery.builder().id(id).build()
//    ).enqueue(object: ApolloCall.Callback<GetPromiseQuery.Data>() {
//      override fun onResponse(response: Response<GetPromiseQuery.Data>) {
//
//        runOnUiThread {
//          promiseDetailActionBar.setCenterText(response.data()?.promise()?.name() ?: "")
//
//          val timestamp = (response.data()?.promise()?.timestamp() ?: "0").toLong()
//          val calendar = Calendar.getInstance()
//          calendar.timeInMillis = timestamp
//
//          promiseDetailDateTimeTextView.text = SimpleDateFormat("MM.dd(EEEE) a hh시 mm분", Locale.getDefault()).format(calendar.time)
//          promiseDetailMapFragment.updateCurrentInfo(response.data()?.promise())
//          promiseDetailUsersFragment.updateCurrentInfo(response.data()?.promise())
//        }
//      }
//
//      override fun onFailure(e: ApolloException) {
//        e.printStackTrace()
//      }
//    })
  }

  private fun isLocationServiceEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
  }

  private fun change() {
    val fragmentTransaction = supportFragmentManager.beginTransaction()

    when (currentDisplayType) {
      DisplayType.Map -> {
        fragmentTransaction.replace(displayChangeLayout.id, promiseDetailMapFragment)
        promiseDetailToggleButton.setImageDrawable(resources.getDrawable(R.drawable.icon_toggle))
      }
      DisplayType.Users -> {
        fragmentTransaction.replace(displayChangeLayout.id, promiseDetailUsersFragment)
        promiseDetailToggleButton.setImageDrawable(resources.getDrawable(R.drawable.icon_map))
      }
    }

    currentDisplayType = if (currentDisplayType == DisplayType.Map) DisplayType.Users else DisplayType.Map
    fragmentTransaction.commit()
  }

  private fun finishWithEditCheck() {
    if (isEditOccurred) {
      setResult(RESULT_CODE_PROMISE_EDIT_OCCURRED)
    }
    finish()
  }

  override fun onBackPressed() {
    finishWithEditCheck()
  }
}
