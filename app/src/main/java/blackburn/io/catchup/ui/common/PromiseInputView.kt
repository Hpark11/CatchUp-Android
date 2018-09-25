package blackburn.io.catchup.ui.common

import android.content.Context
import android.content.res.TypedArray
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import blackburn.io.catchup.R
import kotlinx.android.synthetic.main.view_promise_input.view.*


class PromiseInputView: ConstraintLayout {
  private lateinit var view: View

  enum class InputState {
    APPLIED,
    SEARCH,
    CHOICE
  }

  var text: String
    get() = view.inputContentTextView.text.toString()
    set(value) {
      view.inputContentTextView.text = value
    }

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    init()
    getAttrs(attrs)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    init()
    getAttrs(attrs, defStyle)
  }

  private fun init() {
    view = LayoutInflater.from(context).inflate(R.layout.view_promise_input, this, true)
  }

  private fun getAttrs(attrs: AttributeSet) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PromiseInputView)
    setTypeArray(typedArray)
  }

  private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PromiseInputView, defStyle, 0)
    setTypeArray(typedArray)
  }

  private fun setTypeArray(typedArray: TypedArray) {
    val resId = typedArray.getResourceId(R.styleable.PromiseInputView_sub_image, R.drawable.icon_search)
    view.subIconImageView.setImageDrawable(resources.getDrawable(resId))
    val title = typedArray.getString(R.styleable.PromiseInputView_title_text)
    view.titleTextView.text = title
    val input = typedArray.getString(R.styleable.PromiseInputView_input_text)
    view.inputContentTextView.text = input
    typedArray.recycle()
  }

  fun setupView(state: InputState, input: String) {
    text = input

    when (state) {
      InputState.APPLIED -> {
        view.subIconImageView.setImageDrawable(resources.getDrawable(R.drawable.icon_ok))
        view.promiseLowerLine.setBackgroundColor(resources.getColor(R.color.dark_sky_blue))
      }
      InputState.SEARCH -> {
        view.subIconImageView.setImageDrawable(resources.getDrawable(R.drawable.icon_search))
        view.promiseLowerLine.setBackgroundColor(resources.getColor(R.color.pale_grey))
      }
      InputState.CHOICE -> {
        view.subIconImageView.setImageDrawable(resources.getDrawable(R.drawable.icon_down))
        view.promiseLowerLine.setBackgroundColor(resources.getColor(R.color.pale_grey))
      }
    }
  }
}