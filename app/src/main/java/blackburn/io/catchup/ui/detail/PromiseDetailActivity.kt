package blackburn.io.catchup.ui.detail

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.ui.creation.NewPromiseActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_promise_detail.*
import java.util.*
import javax.inject.Inject
import android.arch.lifecycle.Observer
import blackburn.io.catchup.app.util.StatusBarState
import blackburn.io.catchup.app.util.setStatusBarContentColor
import blackburn.io.catchup.model.PlaceInfo
import com.amazonaws.util.DateUtils
import java.text.SimpleDateFormat
import android.content.BroadcastReceiver
import android.content.IntentFilter

class PromiseDetailActivity : BaseActivity(), HasSupportFragmentInjector {
  companion object {
    const val RESULT_CODE_PROMISE_EDIT_OCCURRED = 9942
    const val LOCATION_UPDATE = "blackburn.io.catchup.ui.detail.location"
  }

  enum class DisplayType {
    Map,
    Users
  }

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

  private lateinit var viewModel: PromiseDetailViewModel
  private var receiver: BroadcastReceiver? = null

  override fun supportFragmentInjector(): AndroidInjector<Fragment> {
    return fragmentInjector
  }

  private var currentDisplayType = DisplayType.Map
  private val promiseDetailMapFragment = PromiseDetailMapFragment()
  private val promiseDetailUsersFragment = PromiseDetailMembersFragment()

  private var isEditOccurred = false
  private lateinit var id: String
  private lateinit var task: TimerTask

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_promise_detail)
    window.statusBarColor = resources.getColor(R.color.dark_sky_blue)
    setStatusBarContentColor(window, StatusBarState.Dark)

    id = intent.getStringExtra("id") ?: ""
    val name = intent.getStringExtra("name") ?: ""

    viewModel = ViewModelProviders.of(this, viewModelFactory)[PromiseDetailViewModel::class.java]
    bindViewModel()

    val fragmentTransaction = supportFragmentManager.beginTransaction()
    fragmentTransaction.add(displayChangeLayout.id, promiseDetailMapFragment)
    fragmentTransaction.add(displayChangeLayout.id, promiseDetailUsersFragment)
    fragmentTransaction.commit()
    change()

    checkPromiseInfo()

    promiseDetailActionBar.setCenterText(name)
    promiseDetailDateTimeTextView.text = SimpleDateFormat(
      "MM.dd(EEEE) a hh시 mm분",
      Locale.getDefault()
    ).format(DateUtils.parseISO8601Date(intent.getStringExtra("dateTime")))

    promiseDetailActionBar.setCenterTextColor(resources.getColor(R.color.md_white_1000))
    promiseDetailActionBar.setFirstLeftButtonClickListener(View.OnClickListener {
      finish()
    })

    promiseDetailToggleButton.setOnClickListener { change() }

    promiseDetailActionBar.setFirstLeftButtonClickListener(
      View.OnClickListener {
      finishWithEditCheck()
    })

    promiseEditButton.setOnClickListener {
      val intent = Intent(this@PromiseDetailActivity, NewPromiseActivity::class.java)
      intent.putExtra("id", id)
      startActivityForResult(intent, 100)
    }

    task = object: TimerTask() {
      override fun run() {
        checkPromiseInfo()
      }
    }

    val timer = Timer()
    timer.schedule(task, 0, 80000)
  }

  override fun onDestroy() {
    super.onDestroy()
    task.cancel()
  }

  private fun checkPromiseInfo() {
    val dateTime = intent.getStringExtra("dateTime") ?: ""

    if (isLocationServiceEnabled() && dateTime.isNotEmpty()) {
      viewModel.loadPromise(id)
    } else {
      Toast.makeText(
        this@PromiseDetailActivity,
        "GPS를 켜지 않아 친구들의 위치정보를 확인할 수 없어요",
        Toast.LENGTH_LONG
      ).show()
    }
  }

  private fun bindViewModel() {
    viewModel.promiseDetail.observe(this, Observer { promise ->
      val name = promise?.name() ?: ""
      val address = promise?.address() ?: ""
      val latitude = promise?.latitude() ?: 0.0
      val longitude = promise?.longitude() ?: 0.0

      promiseDetailActionBar.setCenterText(name)

      promiseDetailDateTimeTextView.text = SimpleDateFormat(
        "MM.dd(EEEE) a hh시 mm분",
        Locale.getDefault()
      ).format(DateUtils.parseISO8601Date(promise?.dateTime()))

      promiseDetailActionBar.setCenterText(name)
      promiseDetailMapFragment.updateDestination(name, PlaceInfo(address, latitude, longitude))
      promiseDetailUsersFragment.updateDestination(PlaceInfo(address, latitude, longitude))
    })

    viewModel.contactList.observe(this, Observer { contacts ->
      val dateTime = intent.getStringExtra("dateTime") ?: ""
      val parsedDateTime = DateUtils.parse(DateUtils.ALTERNATE_ISO8601_DATE_PATTERN, dateTime)
      val current = Calendar.getInstance().timeInMillis

      if (parsedDateTime.time + 3600000 >= current && parsedDateTime.time - 7200000 <= current) {
        promiseDetailMapFragment.updateContacts(contacts ?: listOf())
      } else {
        Toast.makeText(
          this@PromiseDetailActivity,
          "약속 활성화 시간밖엔 위치정보는 볼 수 없어요",
          Toast.LENGTH_LONG
        ).show()
      }

      promiseDetailUsersFragment.updateContacts(contacts ?: listOf())
    })

    viewModel.isOwner.observe(this, Observer { isOwner ->
      promiseEditButton.visibility = if (isOwner == true) View.VISIBLE else View.GONE
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (resultCode) {
      NewPromiseActivity.RESULT_CODE_PROMISE_EDITED -> {
        viewModel.loadPromise(id)
        isEditOccurred = true
      }
    }
  }

  private fun isLocationServiceEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
      manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
  }

  private fun change() {
    val fragmentTransaction = supportFragmentManager.beginTransaction()

    when (currentDisplayType) {
      DisplayType.Map -> {
        fragmentTransaction.show(promiseDetailMapFragment)
        fragmentTransaction.hide(promiseDetailUsersFragment)
        promiseDetailToggleButton.setImageDrawable(resources.getDrawable(R.drawable.icon_toggle))
      }
      DisplayType.Users -> {
        fragmentTransaction.show(promiseDetailUsersFragment)
        fragmentTransaction.hide(promiseDetailMapFragment)
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

  private fun registerReceiver() {
    if (receiver != null) return

    val theFilter = IntentFilter()
    theFilter.addAction(LOCATION_UPDATE)

    receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra("phone")
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        if (intent.action == LOCATION_UPDATE) {
          promiseDetailMapFragment.updateUserLocation(phone, latitude, longitude)
        }
      }
    }

    this.registerReceiver(receiver, theFilter)
  }

  override fun onResume() {
    super.onResume()
    registerReceiver()
  }

  override fun onPause() {
    super.onPause()
    if(receiver != null){
      unregisterReceiver(receiver)
      receiver = null;
    }
  }

  override fun onBackPressed() {
    finishWithEditCheck()
  }
}
