package blackburn.io.catchup.ui.common

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appeaser.sublimepickerlibrary.R
import com.appeaser.sublimepickerlibrary.SublimePicker
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker

class DateTimePicker: DialogFragment() {
  private lateinit var calendarPicker: SublimePicker
  private var callback: Callback? = null

  private var listenerAdapter: SublimeListenerAdapter = object : SublimeListenerAdapter() {
    override fun onCancelled() {
      callback?.onCancelled()
      dismiss()
    }

    override fun onDateTimeRecurrenceSet(
      sublimeMaterialPicker: SublimePicker,
      selectedDate: SelectedDate,
      hourOfDay: Int, minute: Int,
      recurrenceOption: SublimeRecurrencePicker.RecurrenceOption,
      recurrenceRule: String?
    ) {
      callback?.onDateTimeRecurrenceSet(selectedDate, hourOfDay, minute, recurrenceOption, recurrenceRule)
      dismiss()
    }
  }

  fun setCallback(callback: Callback) {
    this.callback = callback
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    activity?.let {
      calendarPicker = it.layoutInflater.inflate(R.layout.sublime_picker, container) as SublimePicker
      calendarPicker.setBackgroundColor(this.resources.getColor(blackburn.io.catchup.R.color.dark_sky_blue))
      val arguments = arguments
      var options: SublimeOptions? = null

      arguments?.let { args ->
        options = args.getParcelable("SUBLIME_OPTIONS")
      }

      calendarPicker.initializePicker(options, listenerAdapter)
    }

    return calendarPicker
  }

  interface Callback {
    fun onCancelled()

    fun onDateTimeRecurrenceSet(
      selectedDate: SelectedDate,
      hourOfDay: Int, minute: Int,
      recurrenceOption: SublimeRecurrencePicker.RecurrenceOption,
      recurrenceRule: String?
    )
  }
}