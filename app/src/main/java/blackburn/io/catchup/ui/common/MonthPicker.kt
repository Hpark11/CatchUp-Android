package blackburn.io.catchup.ui.common

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blackburn.io.catchup.R
import kotlinx.android.synthetic.main.dialog_month_picker.view.*
import java.util.*

class MonthPicker: BottomSheetDialogFragment() {
  companion object {
    fun getInstance(): MonthPicker {
      return MonthPicker()
    }

    private val MAX_YEAR = 2050
    private val MIN_YEAR = 2018
  }

  var listener: DatePickerDialog.OnDateSetListener? = null
  val calendar = Calendar.getInstance()
  var year = 2018
  var month = 1

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.dialog_month_picker, container, false)

    view.confirmButton.setOnClickListener {
      listener?.onDateSet(null, view.yearPicker.value, view.monthPicker.value, 0)
    }

    view.monthPicker.minValue = 1
    view.monthPicker.maxValue = 12
    view.monthPicker.value = month

    val year = calendar.get(Calendar.YEAR)
    view.yearPicker.minValue = MIN_YEAR
    view.yearPicker.maxValue = MAX_YEAR
    view.yearPicker.value = year

    return view
  }

  fun setYearMonth(year: Int, month: Int) {
    this.year = year
    this.month = month
  }
}