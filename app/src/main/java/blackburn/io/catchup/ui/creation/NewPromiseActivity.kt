package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Pair
import android.widget.Toast
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.ui.common.DateTimePicker
import com.afollestad.materialdialogs.MaterialDialog
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import kotlinx.android.synthetic.main.activity_new_promise.*
import android.arch.lifecycle.Observer
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blackburn.io.catchup.app.util.plusAssign
import blackburn.io.catchup.di.module.GlideApp
import blackburn.io.catchup.ui.common.PromiseInputView
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.item_member_selected.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NewPromiseActivity: BaseActivity() {
  companion object {
    val RESULT_CODE_NEW_PROMISE_ADDED = 9999
    val RESULT_CODE_PROMISE_EDITED = 9998
  }

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  private lateinit var viewModel: NewPromiseViewModel

  private var contacts = listOf<String>()

  private val pickerCallback = object : DateTimePicker.Callback {
    override fun onCancelled() {}

    override fun onDateTimeRecurrenceSet(
      selectedDate: SelectedDate,
      hourOfDay: Int,
      minute: Int,
      recurrenceOption: SublimeRecurrencePicker.RecurrenceOption,
      recurrenceRule: String?
    ) {

      val calendar = selectedDate.firstDate
      calendar.set(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DATE),
        hourOfDay,
        minute
      )

      viewModel.dateTimeInput.onNext(calendar.time)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_promise)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[NewPromiseViewModel::class.java]
    bindViewModel()

    val id = intent.getStringExtra("id")
    id?.let { viewModel.loadPromise(it) }

    selectedMembersRecyclerView.apply {
      layoutManager = GridLayoutManager(
        this@NewPromiseActivity,
        1,
        GridLayoutManager.HORIZONTAL,
        false
      )

      adapter = SelectedMembersRecyclerViewAdapter()
    }

    promiseNameInputView.setOnClickListener {
      MaterialDialog.Builder(this)
        .title("이름 짓기")
        .content("생성할 약속의 이름을 입력해주세요")
        .positiveText(R.string.confirm)
        .negativeText(R.string.cancel)
        .inputRangeRes(2, 50, R.color.md_blue_600)
        .input(null, null) { dialog, input ->
          viewModel.name.value = input.toString()
        }.show()
    }

    promiseDateInputView.setOnClickListener {
      val calendarPickerFragment = DateTimePicker()
      calendarPickerFragment.setCallback(pickerCallback)
      val optionsPair = getOptions()

      if (!optionsPair.first) {
        Toast.makeText(
          this@NewPromiseActivity,
          R.string.error_message_datetime_select,
          Toast.LENGTH_SHORT
        ).show()
        return@setOnClickListener
      }

      val bundle = Bundle()
      bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second)
      calendarPickerFragment.arguments = bundle
      calendarPickerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
      calendarPickerFragment.show(supportFragmentManager, "SUBLIME_PICKER")
    }

    promiseAddressInputView.setOnClickListener {
      startActivityForResult(Intent(this, MapSearchActivity::class.java), 0)
    }

    promiseMemberInputView.setOnClickListener {
      val intent = Intent(this, MemberSelectActivity::class.java)
      intent.putExtra("selected", ArrayList(viewModel.contacts.value ?: listOf()))
      startActivityForResult(intent,0)
    }
  }

  private fun bindViewModel() {
    viewModel.name.observe(this, Observer {
      it?.let { name ->
        if (name.isNotEmpty()) {
          promiseNameInputView.setupView(PromiseInputView.InputState.APPLIED, name)
        }
      }
    })

    viewModel.placeInfo.observe(this, Observer {
      it?.let { place ->
        promiseAddressInputView.setupView(PromiseInputView.InputState.APPLIED, place.address)
      }
    })

    viewModel.dateTime.observe(this, Observer {
      it?.let { date ->
        promiseDateInputView.setupView(
          PromiseInputView.InputState.APPLIED,
          SimpleDateFormat("yyyy년 MM월 dd일, hh시 mm분", Locale.KOREA).format(date)
        )
      }
    })

    viewModel.contacts.observe(this, Observer {
      contacts = it ?: listOf()

      if (contacts.isNotEmpty()) {
        viewModel.loadSingleContact(contacts.first())?.let { contactFlowable ->
          disposable += contactFlowable.subscribeBy(
            onNext = { contact ->
              val input = if (contacts.size == 1) contact.nickname
                          else "${contact.nickname}외 ${contacts.size - 1}명"

              promiseMemberInputView.setupView(
                PromiseInputView.InputState.APPLIED,
                input
              )
            },
            onError = {
              it.printStackTrace()
            }
          )
        }
      } else {
        promiseMemberInputView.setupView(
          PromiseInputView.InputState.SEARCH,
          "구성원을 검색해주세요"
        )
      }

      selectedMembersRecyclerView.adapter.notifyDataSetChanged()
    })
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (resultCode) {
      MapSearchActivity.RESULT_CODE_SET_ADDRESS -> {
        data?.let {
          viewModel.placeInput.onNext(
            NewPromiseViewModel.PlaceInfo(
              it.getStringExtra("address"),
              it.getDoubleExtra("latitude", 0.0),
              it.getDoubleExtra("longitude", 0.0)
            )
          )
        }
      }
      MemberSelectActivity.RESULT_CODE_SELECTED -> {
        data?.let {
          viewModel.contactsInput.onNext(it.getStringArrayExtra("selected").toList())
        }
      }
    }
  }

  private fun getOptions(): Pair<Boolean, SublimeOptions> {
    val options = SublimeOptions()
    val displayOptions = SublimeOptions.ACTIVATE_DATE_PICKER or SublimeOptions.ACTIVATE_TIME_PICKER

    options.pickerToShow = SublimeOptions.Picker.DATE_PICKER
    options.setDisplayOptions(displayOptions)
    options.setCanPickDateRange(false)

    return Pair(displayOptions != 0, options)
  }

  inner class SelectedMembersRecyclerViewAdapter
    : RecyclerView.Adapter<SelectedMembersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
      = ViewHolder(LayoutInflater.from(parent.context)
        .inflate(R.layout.item_member_selected, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
      = holder.bind(contacts[position])

    override fun getItemCount(): Int = contacts.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
      fun bind(phone: String) {

        viewModel.loadSingleContact(phone)?.let { contactFlowable ->
          disposable += contactFlowable.subscribeBy(
            onNext = { contact ->
              itemView.selectedMemberTextView.text = contact.nickname
              GlideApp.with(itemView)
                .load(contact.profileImagePath)
                .fitCenter()
                .placeholder(R.drawable.profile_default)
                .into(itemView.selectedMemberImageView)
            },
            onError = {
              it.printStackTrace()
            }
          )
        }
      }
    }
  }
}
