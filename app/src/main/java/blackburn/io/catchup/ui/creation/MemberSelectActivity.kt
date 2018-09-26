package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.model.Contact
import kotlinx.android.synthetic.main.activity_member_select.*
import kotlinx.android.synthetic.main.item_member_select.view.*
import javax.inject.Inject

class MemberSelectActivity: BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: MemberSelectViewModel
  private lateinit var selectedSet: MutableSet<String>
  private var searchedList: List<Contact> = listOf()

  private val confirmUserSelect = View.OnClickListener {
    val intent = Intent()
    intent.putExtra("selected", selectedSet.toTypedArray())
    setResult(RESULT_CODE_SELECTED, intent)
    finish()
  }

  private val cancelUserSelect = View.OnClickListener {
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_member_select)
    setStatusBarTranslucent(true)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[MemberSelectViewModel::class.java]
    bindViewModel()

    selectedSet = intent.getStringArrayListExtra("selected").toMutableSet()

    memberSelectRecyclerView.apply {
      layoutManager = LinearLayoutManager(this@MemberSelectActivity)
      adapter = MemberSelectRecyclerViewAdapter()
    }

    memberSelectActionBar.apply {
      setSecondRightTextButtonListener(confirmUserSelect)
      setFirstLeftButtonClickListener(cancelUserSelect)
      setRightSecondTextColor(resources.getColor(R.color.dark_sky_blue))
    }

    searchContactEditText.addTextChangedListener(object: TextWatcher {
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//        searchedList = viewModel.contactList.value?.filter {
//          it.nickname.toLowerCase().contains(s.toString().toLowerCase())
//        } ?: listOf()
        memberSelectRecyclerView.adapter.notifyDataSetChanged()
      }

      override fun afterTextChanged(s: Editable?) {}
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    })

    viewModel.loadContactList()
  }

  private fun bindViewModel() {
    viewModel.getContacts().observe(this, Observer {
      searchedList = it ?: listOf()
      memberSelectRecyclerView.adapter.notifyDataSetChanged()
    })
  }

  inner class MemberSelectRecyclerViewAdapter: RecyclerView.Adapter<MemberSelectRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
      = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_member_select, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
      = holder.bind(searchedList[position])

    override fun getItemCount(): Int = searchedList.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
      fun bind(contactItem: Contact) {
        itemView.userNameTextView.text = contactItem.nickname
        itemView.isSelected = selectedSet.contains(contactItem.phone)
        if (itemView.isSelected) {
          itemView.checkMarkImageView.setImageDrawable(resources.getDrawable(R.drawable.icon_ok))
        } else {
          itemView.checkMarkImageView.setImageDrawable(resources.getDrawable(R.drawable.image_none))
        }


        itemView.setOnClickListener {
          if (it.isSelected) {
            selectedSet.remove(contactItem.phone)
            itemView.checkMarkImageView.setImageDrawable(resources.getDrawable(R.drawable.image_none))
          } else {
            selectedSet.add(contactItem.phone)
            itemView.checkMarkImageView.setImageDrawable(resources.getDrawable(R.drawable.icon_ok))
          }

          it.isSelected = !it.isSelected
        }
      }
    }
  }

  companion object {
    val RESULT_CODE_SELECTED = 9997
  }
}
