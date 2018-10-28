package blackburn.io.catchup.ui.creation

import android.app.ActivityOptions
import android.app.AlertDialog
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
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.util.plusAssign
import blackburn.io.catchup.di.module.GlideApp
import blackburn.io.catchup.model.PlaceInfo
import blackburn.io.catchup.ui.common.PromiseInputView
import com.google.firebase.firestore.FirebaseFirestore
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.item_member_selected.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NewPromiseActivity : BaseActivity() {
  companion object {
    const val RESULT_CODE_PROMISE_ADDED = 9999
    const val RESULT_CODE_PROMISE_EDITED = 9998
  }

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  private lateinit var viewModel: NewPromiseViewModel

  private var contacts = listOf<String>()

  var dialog: AlertDialog? = null
  private var calendar = Calendar.getInstance()

  private val timeSelectedCallback = object : DateTimePicker.Callback {
    override fun onCancelled() {}

    override fun onDateTimeRecurrenceSet(
      selectedDate: SelectedDate?,
      hourOfDay: Int,
      minute: Int,
      recurrenceOption: SublimeRecurrencePicker.RecurrenceOption,
      recurrenceRule: String?
    ) {

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

  private val dateSelectedCallback = object : DateTimePicker.Callback {
    override fun onCancelled() {}

    override fun onDateTimeRecurrenceSet(
      selectedDate: SelectedDate?,
      hourOfDay: Int,
      minute: Int,
      recurrenceOption: SublimeRecurrencePicker.RecurrenceOption,
      recurrenceRule: String?
    ) {

      calendar = selectedDate?.firstDate ?: Calendar.getInstance()
      showDateTimePicker(SublimeOptions.ACTIVATE_TIME_PICKER, timeSelectedCallback)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_promise)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[NewPromiseViewModel::class.java]
    bindViewModel()

    val id = intent.getStringExtra("id")
    id?.let {
      viewModel.loadPromise(it)
      newPromiseConfirmButton.background = resources.getDrawable(R.drawable.btn_modify)
    }

    newPromiseActionBar.setFirstLeftButtonClickListener(View.OnClickListener {
      finish()
    })

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
      showDateTimePicker(SublimeOptions.ACTIVATE_DATE_PICKER, dateSelectedCallback)
    }

    promiseAddressInputView.setOnClickListener {
      startActivityForResult(Intent(this, MapSearchActivity::class.java), 0)
    }

    newPromiseConfirmButton.setOnClickListener { view ->
      dialog = SpotsDialog.Builder().setContext(this@NewPromiseActivity).build()

      val reasons = mutableListOf<String>()
      if (viewModel.name.value.isNullOrEmpty()) {
        reasons.add("이름")
      }

      if (viewModel.dateTime.value == null) {
        reasons.add("일시")
      }

      if (viewModel.placeInfo.value == null) {
        reasons.add("장소")
      }

      if (reasons.isNotEmpty()) {
        Toast.makeText(this@NewPromiseActivity, "${reasons.joinToString(separator = ",")} 입력 필요", Toast.LENGTH_LONG).show()
        return@setOnClickListener
      }

      dialog?.show()

      disposable += viewModel.makePromise(id).observeOn(AndroidSchedulers.mainThread()).subscribeBy(
        onSuccess = { data ->
          data.updateCatchUpPromise()?.let { promise ->
            notify(id != null, promise.contacts() ?: listOf())
            confirmPromise(id != null, promise.id())
          }
        },
        onError = {
          it.printStackTrace()
        },
        onComplete = {
          dialog?.dismiss()
        }
      )
    }
  }

  private fun showDateTimePicker(option: Int, callback: DateTimePicker.Callback) {
    val calendarPickerFragment = DateTimePicker()
    calendarPickerFragment.setCallback(callback)
    val optionsPair = getOptions(option)

    if (!optionsPair.first) {
      Toast.makeText(
        this@NewPromiseActivity,
        R.string.error_message_datetime_select,
        Toast.LENGTH_SHORT
      ).show()
      return
    }

    val bundle = Bundle()
    bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second)
    calendarPickerFragment.arguments = bundle
    calendarPickerFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0)
    calendarPickerFragment.show(supportFragmentManager, "SUBLIME_PICKER")
  }

  private fun notify(isEdit: Boolean, contacts: List<String>) {
    val dataMap = mutableMapOf<String, Any>()
    dataMap[Define.FIELD_TITLE] = if (isEdit) "변경된 약속 알림" else "새로운 약속 알림"
    dataMap[Define.FIELD_MESSAGE] = "일시: ${promiseDateInputView.text}, 장소: ${promiseAddressInputView.text}"

    disposable += viewModel.loadContacts(contacts).observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onSuccess = { contactList ->
          dataMap[Define.FIELD_PUSH_TOKENS] = contactList.mapNotNull { it.pushToken() }

          FirebaseFirestore.getInstance().collection(Define.COLLECTION_MESSAGES)
            .document(UUID.randomUUID().toString()).set(dataMap)
            .addOnSuccessListener {
              Toast.makeText(this@NewPromiseActivity, "알림 메세지를 보냈어요", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
              Toast.makeText(this@NewPromiseActivity, "전송 오류", Toast.LENGTH_LONG).show()
            }
        }
      )
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
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (resultCode) {
      MapSearchActivity.RESULT_CODE_SET_ADDRESS -> {
        data?.let {
          viewModel.placeInput.onNext(
            PlaceInfo(
              it.getStringExtra("address"),
              it.getDoubleExtra("latitude", 0.0),
              it.getDoubleExtra("longitude", 0.0)
            )
          )
        }
      }
    }
  }

  private fun confirmPromise(isEdit: Boolean, id: String = "") {
    val options = ActivityOptions.makeSceneTransitionAnimation(this)
    val intent = Intent(this@NewPromiseActivity, PromiseConfirmActivity::class.java)

    intent.putExtra("isEdit", isEdit)
    intent.putExtra("name", promiseNameInputView.text)
    intent.putExtra("dateTime", promiseDateInputView.text)
    intent.putExtra("location", promiseAddressInputView.text)
    intent.putExtra("id", id)
    startActivity(intent, options.toBundle())

    val resultIntent = Intent()
    resultIntent.putExtra("id", id)
    setResult(if (isEdit) RESULT_CODE_PROMISE_EDITED else RESULT_CODE_PROMISE_ADDED, resultIntent)
    finish()
  }

  private fun getOptions(option: Int): Pair<Boolean, SublimeOptions> {
    val options = SublimeOptions()

    options.pickerToShow =
      if (option == SublimeOptions.ACTIVATE_DATE_PICKER) SublimeOptions.Picker.DATE_PICKER
      else SublimeOptions.Picker.TIME_PICKER
    options.setDisplayOptions(option)
    options.setCanPickDateRange(false)

    return Pair(option != 0, options)
  }

  inner class SelectedMembersRecyclerViewAdapter
    : RecyclerView.Adapter<SelectedMembersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
      LayoutInflater.from(parent.context)
        .inflate(
          R.layout.item_member_selected,
          parent,
          false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(contacts[position])

    override fun getItemCount(): Int = contacts.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

  override fun onDestroy() {
    super.onDestroy()
    dialog?.dismiss()
    dialog = null
  }
}
