package blackburn.io.catchup.ui

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.arch.lifecycle.Observer
import android.content.Context
import android.location.LocationManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.util.plusAssign
import blackburn.io.catchup.di.module.GlideApp
import blackburn.io.catchup.service.android.LocationTrackingService
import blackburn.io.catchup.ui.common.MonthPicker
import blackburn.io.catchup.ui.creation.NewPromiseActivity
import blackburn.io.catchup.ui.detail.PromiseDetailActivity
import com.amazonaws.amplify.generated.graphql.ListCatchUpPromisesByContactQuery
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_member_selected_min.view.*
import kotlinx.android.synthetic.main.item_promise.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: MainViewModel

  private var monthPicker: MonthPicker? = null
  private val current = Calendar.getInstance()
  private var isServiceRunning = false

  private val dateSetListener = DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
    current.set(Calendar.YEAR, year)
    current.set(Calendar.MONTH, month - 1)
    formatCurrent(current)
    viewModel.filterInput.onNext(Pair(year, month))
    monthPicker?.dismiss()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
    bindViewModel()

    val phone = intent.getStringExtra("phone")
    formatCurrent(current)

    promisesRecyclerView.adapter = PromisesRecyclerViewAdapter(phone)
    promisesRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

    addPromiseButton.setOnClickListener {
      val intent = Intent(this, NewPromiseActivity::class.java)
      intent.putExtra("phone", intent.getStringExtra("phone"))
      startActivityForResult(intent, 100)
    }

    monthSelectLayout.setOnClickListener {
      monthPicker = MonthPicker.getInstance()
      monthPicker?.setYearMonth(current.get(Calendar.YEAR), current.get(Calendar.MONTH) + 1)
      monthPicker?.listener = dateSetListener
      monthPicker?.show(supportFragmentManager,"");
    }

    viewModel.loadPromiseList(current.get(Calendar.YEAR), current.get(Calendar.MONTH) + 1)

    statusCheck()

    if (isGooglePlayServicesAvailable()) {
      if (!isServiceRunning) {
//        stopService(Intent())
        val intent = Intent(this@MainActivity, LocationTrackingService::class.java)
        startService(intent)
        isServiceRunning = true
      }
    }
  }

  private fun statusCheck() {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

      AlertDialog.Builder(this)
        .setMessage("예정 2시간 이내의 모임약속 정보를 확인하기 위해서는 친구들과의 GPS로 위치공유가 필요하니 켜두세요~ \n" +
          "(약속 2시간 이내가 아닌 시간에는 절대 위치 추적을 하지 않을거에요)")
        .setCancelable(false)
        .setPositiveButton("Yes") { dialog, id ->
          startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        .setNegativeButton("No") { dialog, id ->
          dialog.cancel()
        }
        .create()
        .show()
    }
  }

  private fun isGooglePlayServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
    if (status != ConnectionResult.SUCCESS) {
      if (googleApiAvailability.isUserResolvableError(status)) {
        googleApiAvailability.getErrorDialog(this, status, 2404).show()
      }
      return false
    }
    return true
  }

  private fun bindViewModel() {
    viewModel.filteredList.observe(this, Observer { promiseList ->
      promiseList?.let {
        mainPlaceHolderView.visibility = if (promiseList.isEmpty()) View.VISIBLE else View.GONE
        (promisesRecyclerView.adapter as PromisesRecyclerViewAdapter).setPromises(promiseList)
      }
    })
  }

  private fun formatCurrent(calendar: Calendar) {
    monthSelectTextView.text = SimpleDateFormat("yyyy.MM", Locale.KOREA).format(calendar.time)
  }

  private inner class PromisesRecyclerViewAdapter(
    private val currentUserPhone: String
  ) : RecyclerView.Adapter<PromisesRecyclerViewAdapter.ViewHolder>() {
    private var promiseList = listOf<ListCatchUpPromisesByContactQuery.ListCatchUpPromisesByContact>()

    fun setPromises(list: List<ListCatchUpPromisesByContactQuery.ListCatchUpPromisesByContact>) {
      promiseList = list
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
      = ViewHolder(LayoutInflater.from(this@MainActivity)
      .inflate(R.layout.item_promise, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(promiseList[position])
    }

    override fun getItemCount(): Int = promiseList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      fun bind(promise: ListCatchUpPromisesByContactQuery.ListCatchUpPromisesByContact) {
        itemView.promiseNameTextView.text = promise.name()
        itemView.promiseAddressTextView.text = promise.address()

        val current = Date()
        val promiseDate = com.amazonaws.util.DateUtils.parseISO8601Date(promise.dateTime())

        if (current > promiseDate) {
          itemView.alpha = 0.4f
          itemView.timeLeftTextView.text = "지난 약속"
          itemView.promiseDateCardView.setCardBackgroundColor(resources.getColor(R.color.silver))
        } else {
          itemView.alpha = 1f
          val difference = promiseDate.time - current.time

          val diffDays = difference / Define.DIFF_DAYS
          if (diffDays > 0) {
            itemView.timeLeftTextView.text = "D - $diffDays"
            itemView.promiseDateCardView.setCardBackgroundColor(resources.getColor(R.color.dark_sky_blue_two))
          } else {
            val diffHours = difference / Define.DIFF_HOURS
            if (diffHours > 0) {
              itemView.timeLeftTextView.text = "$diffHours 시간 전"
            } else {
              val diffMinutes = difference / Define.DIFF_MINUTES
              itemView.timeLeftTextView.text = "$diffMinutes 분 전"
            }
            itemView.promiseDateCardView.setCardBackgroundColor(resources.getColor(R.color.dark_sky_blue))
          }
        }

        try {
          itemView.promiseDateTimeTextView.text = SimpleDateFormat("a hh시 MM분", Locale.getDefault()).format(promiseDate)
          itemView.promiseDateTextView.text = DateFormat.format("dd", promiseDate)
          itemView.promiseDayTextView.text = DateFormat.format("EEEE", promiseDate)
        } catch (e: Exception) {
          e.printStackTrace()
        }

        itemView.setOnClickListener {
          val intent = Intent(it.context, PromiseDetailActivity::class.java)
          intent.putExtra("name", promise.name())
          intent.putExtra("dateTime", promise.dateTime())
          intent.putExtra("id", promise.id())
          startActivityForResult(intent, 100)
        }

        itemView.membersRecyclerView.apply {
          adapter = MembersRecyclerViewAdapter(promise.contacts()?.filter { !it.equals(currentUserPhone) } ?: listOf())
          layoutManager = GridLayoutManager(itemView.context, 1, GridLayoutManager.HORIZONTAL, false)
        }
      }
    }

    inner class MembersRecyclerViewAdapter(
      private var list: List<String>
    ) : RecyclerView.Adapter<MembersRecyclerViewAdapter.ViewHolder>() {

      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_member_selected_min, parent, false))

      override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

      override fun getItemCount(): Int = list.size

      inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(phone: String) {
          disposable += viewModel.loadContact(phone).subscribeBy(
            onNext = { response ->

              GlideApp.with(itemView)
                .load(response.data()?.catchUpContact?.profileImagePath())
                .fitCenter()
                .placeholder(R.drawable.profile_default)
                .into(itemView.selectedMemberImageView)
            }
          )
        }
      }
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finishAffinity()
  }
}
