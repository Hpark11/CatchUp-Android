package blackburn.io.catchup.ui.detail

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
import blackburn.io.catchup.service.app.SharedPrefService
import com.afollestad.materialdialogs.MaterialDialog
import com.amazonaws.amplify.generated.graphql.BatchGetCatchUpContactsQuery
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.fragment_promise_detail_members.view.*
import kotlinx.android.synthetic.main.item_promise_detail.view.*
import java.util.*
import javax.inject.Inject

class PromiseDetailMembersFragment: BaseFragment() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var sharedPrefService: SharedPrefService

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

  private fun notify(token: String, message: String) {
    val dataMap = mutableMapOf<String, Any>()
    dataMap[Define.FIELD_TITLE] = "${sharedPrefService.nickname}님의 메세지"
    dataMap[Define.FIELD_PUSH_TOKENS] = listOf(token)
    dataMap[Define.FIELD_MESSAGE] = message

    FirebaseFirestore.getInstance().collection(Define.COLLECTION_MESSAGES)
      .document(UUID.randomUUID().toString()).set(dataMap)
      .addOnSuccessListener {
        Toast.makeText(this.requireContext(), "알림 메세지를 보냈어요", Toast.LENGTH_LONG).show()
      }
      .addOnFailureListener {
        Toast.makeText(this.requireContext(), "전송 오류", Toast.LENGTH_LONG).show()
      }
  }

  inner class CurrentMembersRecyclerViewAdapter
    : RecyclerView.Adapter<CurrentMembersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
      = ViewHolder(LayoutInflater.from(parent.context)
      .inflate(R.layout.item_promise_detail, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
      = holder.bind(contactList[position])

    override fun getItemCount(): Int = contactList.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
      fun bind(contact: BatchGetCatchUpContactsQuery.BatchGetCatchUpContact) {

        GlideApp.with(itemView).load(contact.profileImagePath())
          .fitCenter()
          .placeholder(R.drawable.profile_default)
          .into(itemView.profileImageView)
        itemView.nicknameTextView.text = contact.nickname()

        itemView.notifyPromiseButton.setOnClickListener { view ->
          val pushToken = contact.pushToken()
          if (pushToken == null) {
            Toast.makeText(view.context, "캐치업 회원이 아니라서 알림을 보낼 수 없어요 ㅠㅠ", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
          } else {
            MaterialDialog.Builder(itemView.context)
              .title("알림")
              .content("알림 메세지를 입력하세요")
              .positiveText(R.string.confirm)
              .negativeText(R.string.cancel)
              .inputRangeRes(2, 80, R.color.dark_sky_blue)
              .input(null, null) { dialog, input ->
                notify(pushToken, input.toString())
              }.show()
          }
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

        val distance = loadDistance(contact.latitude(), contact.longitude())

        if (distance >= Define.DISTANCE_UPPERBOUND) {
          itemView.dueTimeTextView.text = "행방불명"
          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.warm_pink))
          itemView.expectedTimeTextView.text = "좌표 로드 실패"
        } else if (distance <= Define.DISTANCE_LOWERBOUND) {
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