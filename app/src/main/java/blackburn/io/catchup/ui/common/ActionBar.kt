package blackburn.io.catchup.ui.common

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import blackburn.io.catchup.R
import kotlinx.android.synthetic.main.view_action_bar.view.*

class ActionBar: LinearLayout {
  private lateinit var view: View

  private var firstLeft = false
  private var secondLeft = false
  private var firstRight = false
  private var secondRight = false

  val centerImageX: Float
    get() = view.centerImageView.x

  val centerImageY: Float
    get() = view.centerImageView.y

  enum class ButtonKey {
    firstLeft,
    secondLeft,
    firstRight,
    secondRight
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
    view = LayoutInflater.from(context).inflate(R.layout.view_action_bar, this, true)
  }

  private fun setItemsVisibility() {
    view.firstLeftSideBarButton.visibility = if (firstLeft) View.VISIBLE else View.INVISIBLE
    view.secondLeftSideBarButton.visibility = if (secondLeft) View.VISIBLE else View.INVISIBLE
    view.firstRightSideBarButton.visibility = if (firstRight) View.VISIBLE else View.INVISIBLE
    view.secondRightSideBarButton.visibility = if (secondRight) View.VISIBLE else View.INVISIBLE
  }

  fun setActionBarItems(firstLeft: Boolean, secondLeft: Boolean, firstRight: Boolean, secondRight: Boolean) {
    this.firstLeft = firstLeft
    this.secondLeft = secondLeft
    this.firstRight = firstRight
    this.secondRight = secondRight
    setItemsVisibility()
  }

  private fun getAttrs(attrs: AttributeSet) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ActionBar)
    setTypeArray(typedArray)
  }

  private fun getAttrs(attrs: AttributeSet, defStyle: Int) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ActionBar, defStyle, 0)
    setTypeArray(typedArray)
  }

  private fun setTypeArray(typedArray: TypedArray) {
    val title = typedArray.getString(R.styleable.ActionBar_center_title_text)
    view.centerTextView.text = title

    val resId = typedArray.getResourceId(R.styleable.ActionBar_left_first_image, R.drawable.image_none)
    view.firstLeftSideBarButton.setImageDrawable(resources.getDrawable(resId))

    val useLeftFirst = typedArray.getBoolean(R.styleable.ActionBar_use_left_first, false)
    view.firstLeftSideBarButton.visibility = if (useLeftFirst) View.VISIBLE else View.INVISIBLE

    val useLeftSecond = typedArray.getBoolean(R.styleable.ActionBar_use_left_second, false)
    view.secondLeftSideBarButton.visibility = if (useLeftSecond) View.VISIBLE else View.INVISIBLE

    val useRightFirst = typedArray.getBoolean(R.styleable.ActionBar_use_right_first, false)
    view.firstRightSideBarButton.visibility = if (useRightFirst) View.VISIBLE else View.INVISIBLE

    val useRightSecond = typedArray.getBoolean(R.styleable.ActionBar_use_right_second, false)

    if (useRightSecond) {
      val rightSecondType = typedArray.getString(R.styleable.ActionBar_right_second_type)
      when (rightSecondType) {
        "text" -> {
          view.secondRightSideBarButton.visibility = View.GONE
          view.secondRightSideBarTextButton.visibility = View.VISIBLE
          val rightSecondText = typedArray.getString(R.styleable.ActionBar_right_second_text)
          view.secondRightSideBarTextButton.text = rightSecondText
        }
        "image" -> {
          view.secondRightSideBarButton.visibility = View.VISIBLE
          view.secondRightSideBarTextButton.visibility = View.GONE
          val rightSecondImageId = typedArray.getResourceId(R.styleable.ActionBar_right_second_image, R.drawable.icon_search)
          view.firstLeftSideBarButton.setImageDrawable(resources.getDrawable(rightSecondImageId))
        }
      }
    }

    typedArray.recycle()
  }

  fun setFirstLeftButtonClickListener(onClickListener: View.OnClickListener) {
    view.firstLeftSideBarButton.setOnClickListener(onClickListener)
  }

  fun setSecondLeftButtonClickListener(onClickListener: View.OnClickListener) {
    view.secondLeftSideBarButton.setOnClickListener(onClickListener)
  }

  fun setFirstRightButtonClickListener(onClickListener: View.OnClickListener) {
    view.firstRightSideBarButton.setOnClickListener(onClickListener)
  }

  fun setSecondRightButtonClickListener(onClickListener: View.OnClickListener) {
    view.secondRightSideBarButton.setOnClickListener(onClickListener)
  }

  fun setFirstLeftButtonDrawable(drawable: Drawable) {
    view.firstLeftSideBarButton.setImageDrawable(drawable)
  }

  fun setFirstLeftButtonDrawable(resId: Int) {
    view.firstLeftSideBarButton.setImageDrawable(resources.getDrawable(resId))
  }

  fun setSecondLeftButtonDrawable(drawable: Drawable) {
    view.secondLeftSideBarButton.setImageDrawable(drawable)
  }

  fun setSecondLeftButtonDrawable(resId: Int) {
    view.secondLeftSideBarButton.setImageDrawable(resources.getDrawable(resId))
  }

  fun setFirstRightButtonDrawable(drawable: Drawable) {
    view.firstRightSideBarButton.setImageDrawable(drawable)
  }

  fun setFirstRightButtonDrawable(resId: Int) {
    view.firstRightSideBarButton.setImageDrawable(resources.getDrawable(resId))
  }

  fun setSecondRightButtonDrawable(drawable: Drawable) {
    view.secondRightSideBarButton.setImageDrawable(drawable)
  }

  fun setSecondRightButtonDrawable(resId: Int) {
    view.secondRightSideBarButton.setImageDrawable(resources.getDrawable(resId))
  }

  fun setCenterImageDrawable(imageUrl: String) {
    if (imageUrl.isEmpty()) {
      view.centerImageView.setVisibility(View.GONE)
    } else {
      view.centerImageView.setVisibility(View.VISIBLE)
    }
  }

  fun setSecondRightTextButtonListener(onClickListener: View.OnClickListener) {
    view.secondRightSideBarButton.setOnClickListener(null)
    view.secondRightSideBarTextButton.setOnClickListener(onClickListener)
  }

  var secondRightText
    get() = view.secondRightSideBarTextButton.text.toString()
    set(value) {
      view.secondRightSideBarTextButton.text = value
    }

  fun setCenterText(text: String?) {
    view.centerTextView.text = text
  }

  fun setCenterTextColor(color: Int) {
    view.centerTextView.setTextColor(color)
  }

  fun setRightSecondTextColor(color: Int) {
    view.secondRightSideBarTextButton.setTextColor(color)
  }

  fun enableButton(key: ButtonKey, isEnabled: Boolean) {
    when (key) {
      ActionBar.ButtonKey.firstLeft -> view.firstLeftSideBarButton.setEnabled(isEnabled)
      ActionBar.ButtonKey.secondLeft -> view.secondLeftSideBarButton.setEnabled(isEnabled)
      ActionBar.ButtonKey.firstRight -> view.firstRightSideBarButton.setEnabled(isEnabled)
      ActionBar.ButtonKey.secondRight -> {
        view.secondRightSideBarTextButton.setEnabled(isEnabled)
        view.secondRightSideBarButton.setEnabled(isEnabled)
      }
    }
  }

  fun setSecondRightEnabled(enabled: Boolean) {
    view.secondRightSideBarButton.setEnabled(enabled)
    view.secondRightSideBarTextButton.setEnabled(enabled)

    view.secondRightSideBarTextButton.setTextColor(if (enabled) Color.BLACK else Color.LTGRAY)
  }
}
