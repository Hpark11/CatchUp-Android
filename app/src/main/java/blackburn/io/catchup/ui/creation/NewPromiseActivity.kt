package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.RecyclerView
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.ui.common.DateTimePicker
import com.afollestad.materialdialogs.MaterialDialog
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import kotlinx.android.synthetic.main.activity_new_promise.*
import java.util.*
import javax.inject.Inject

class NewPromiseActivity : BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: NewPromiseViewModel

  private val pickerCallback = object : DateTimePicker.Callback {
    override fun onCancelled() {}

    override fun onDateTimeRecurrenceSet(
      selectedDate: SelectedDate,
      hourOfDay: Int, minute: Int,
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
//      timestamp = calendar.timeInMillis.toString()
//      promiseDateInputView.setupView(PromiseInputView.InputState.APPLIED, formattedDateText(selectedDate.firstDate, hourOfDay, minute))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_promise)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[NewPromiseViewModel::class.java]
    bindViewModel()

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
      intent.putExtra("selected", viewModel.contacts.value?.toTypedArray())
      startActivityForResult(intent,0)
    }
  }

  private fun bindViewModel() {

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (resultCode) {
      MapSearchActivity.RESULT_CODE_SET_ADDRESS -> {

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

//  class SelectedUsersRecyclerViewAdapter(
//    private var list: List<ContactItem>
//  ): RecyclerView.Adapter<SelectedUsersRecyclerViewAdapter.ViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
//      = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user_selected, parent, false))
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int)
//      = holder.bind(list[position])
//
//    override fun getItemCount(): Int = list.size
//
//    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//      fun bind(userItem: ContactItem) {
//        itemView.selectedUserTextView.text = userItem.nickname
//        GlideApp.with(itemView).load(userItem.imagePath).fitCenter().placeholder(R.drawable.image_place_holder).into(itemView.selectedUserImageView)
//      }
//    }
//
//    fun setItems(list: List<ContactItem>) {
//      this.list = list
//      notifyDataSetChanged()
//    }
//  }

  companion object {
    val RESULT_CODE_NEW_PROMISE_ADDED = 9999
    val RESULT_CODE_PROMISE_EDITED = 9998
  }
}
