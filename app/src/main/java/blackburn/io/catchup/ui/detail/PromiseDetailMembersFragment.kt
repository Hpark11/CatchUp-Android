package blackburn.io.catchup.ui.detail

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.location.Location
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseFragment
import blackburn.io.catchup.app.Define
import blackburn.io.catchup.app.util.plusAssign
import blackburn.io.catchup.di.module.GlideApp
import blackburn.io.catchup.model.PlaceInfo
import com.afollestad.materialdialogs.MaterialDialog
import com.amazonaws.amplify.generated.graphql.BatchGetCatchUpContactsQuery
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_promise_detail_members.view.*
import kotlinx.android.synthetic.main.item_promise_detail.view.*
import javax.inject.Inject

class PromiseDetailMembersFragment: BaseFragment() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: PromiseDetailMembersViewModel

  private var adapter: CurrentMembersRecyclerViewAdapter? = null
  private var destination = Location("")
  private var contactList = listOf<BatchGetCatchUpContactsQuery.BatchGetCatchUpContact>()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_promise_detail_members, container, false)
    viewModel = ViewModelProviders.of(this, viewModelFactory)[PromiseDetailMembersViewModel::class.java]

    adapter = CurrentMembersRecyclerViewAdapter()

    view.currentMembersRecyclerView.adapter = adapter
    view.currentMembersRecyclerView.layoutManager = LinearLayoutManager(this.context)

    destination.latitude = Define.DEFAULT_LATITUDE
    destination.longitude = Define.DEFAULT_LONGITUDE
    return view
  }

  fun updateDestination(place: PlaceInfo) {
    destination.latitude = place.latitude
    destination.longitude = place.longitude

    contactList.sortedByDescending { loadDistance(it.latitude(), it.longitude()) }
    adapter?.notifyDataSetChanged()
  }

  fun updateContacts(contacts: List<BatchGetCatchUpContactsQuery.BatchGetCatchUpContact>) {
    contactList = contacts.sortedByDescending { loadDistance(it.latitude(), it.longitude()) }
    adapter?.notifyDataSetChanged()
  }

  private fun loadDistance(latitude: Double?, longitude: Double?): Float {
    val locationFrom = Location("")
    locationFrom.latitude = latitude ?: Define.DEFAULT_LATITUDE
    locationFrom.longitude = longitude ?: Define.DEFAULT_LONGITUDE
    return locationFrom.distanceTo(destination)
  }

  inner class CurrentMembersRecyclerViewAdapter
    : RecyclerView.Adapter<CurrentMembersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
      = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_promise_detail, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
      = holder.bind(contactList[position])

    override fun getItemCount(): Int = contactList.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
      fun bind(contact: BatchGetCatchUpContactsQuery.BatchGetCatchUpContact) {

        GlideApp.with(itemView).load(contact.profileImagePath()).fitCenter().placeholder(R.drawable.profile_default).into(itemView.profileImageView)
        itemView.nicknameTextView.text = contact.nickname()

        itemView.notifyPromiseButton.setOnClickListener { view ->
          if (contact.pushToken() == null) {
            Toast.makeText(view.context, "캐치업 회원이 아니라서 알림을 보낼 수 없어요 ㅠㅠ", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
          }

          MaterialDialog.Builder(itemView.context)
            .title("알림")
            .content("알림 메세지를 입력하세요")
            .positiveText(R.string.confirm)
            .negativeText(R.string.cancel)
            .inputRangeRes(2, 80, R.color.dark_sky_blue)
            .input(null, null) { dialog, input ->
//              App.apolloClient.query(
//                SendPushQuery.builder()
//                  .pushTokens(itemList.mapNotNull { if (it.pushToken.isEmpty()) null else it.pushToken })
//                  .title("친구로부터 알림")
//                  .body(input.toString())
//                  .build()
//              ).enqueue(object : ApolloCall.Callback<SendPushQuery.Data>() {
//                override fun onResponse(response: Response<SendPushQuery.Data>) {
//                }
//
//                override fun onFailure(e: ApolloException) {
//                }
//              })
            }.show()
        }

        viewModel.loadSingleContact(contact.phone())?.let { contactFlowable ->
          disposable += contactFlowable.subscribeBy(
            onNext = { localContact ->
              itemView.nicknameTextView.text = localContact.nickname
            },
            onError = {
              it.printStackTrace()
            }
          )
        }

        val muta: MutableLiveData<String> = MutableLiveData()
        muta.observe(this@PromiseDetailMembersFragment, Observer {  })

        val distance = loadDistance(contact.latitude(), contact.longitude())

        if (distance >= Define.DISTANCE_UPPERBOUND) {
          itemView.dueTimeTextView.text = "행방불명"
          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.warm_pink))
          itemView.expectedTimeTextView.text = "GPS 차단 중"
        } else if (distance <= Define.DISTANCE_UPPERBOUND) {
          itemView.dueTimeTextView.text = "도착"
          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.warm_pink))
          itemView.expectedTimeTextView.text = String.format("약 %.2f km", distance / 1000f)
        } else {
          itemView.dueTimeTextView.text = "이동 중"
          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.periwinkle_blue))
          itemView.expectedTimeTextView.text = String.format("약 %.2f km", distance / 1000f)
        }
      }
    }
  }
}