package blackburn.io.catchup.ui.detail

import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import blackburn.io.catchup.app.BaseFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*

class PromiseDetailMembersFragment: BaseFragment() {
//  private var adapter: UserCurrentStateRecyclerViewAdapter? = null
//  private var itemList = listOf<Item>()
//  private var arrival: Long = 0L

//  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//    val view = inflater.inflate(R.layout.fragment_promise_detail_users, container, false)
//    adapter = UserCurrentStateRecyclerViewAdapter()
//
//    view.currentMembersRecyclerView.adapter = adapter
//    view.currentMembersRecyclerView.layoutManager = LinearLayoutManager(this.context)
//    return view
//  }
//
//  fun updateCurrentInfo(promise: GetPromiseQuery.Promise?) {
//    promise?.let {
//      val locationTo = Location("")
//      locationTo.latitude = it.latitude() ?: Define.DEFAULT_LATITUDE
//      locationTo.longitude = it.longitude() ?: Define.DEFAULT_LONGITUDE
//
//      val sdf = SimpleDateFormat("a hh시 mm분", Locale.getDefault())
//      arrival = promise.timestamp()?.toLong() ?: Calendar.getInstance().timeInMillis
//      val calendar = Calendar.getInstance()
//      calendar.timeInMillis = arrival
//      val currentDate = sdf.format(calendar.time)
//
//      itemList = listOf(it.pockets()?.map {
//        val locationFrom = Location("")
//        locationFrom.latitude = it.latitude() ?: Define.DEFAULT_LATITUDE
//        locationFrom.longitude = it.longitude() ?: Define.DEFAULT_LONGITUDE
//        Item(TYPE_USER, it.phone(), it.nickname() ?: "", locationFrom.distanceTo(locationTo), it.profileImagePath() ?: "", it.pushToken() ?: "")
//      }, listOf(Item(TYPE_DEADLINE, "", currentDate, Define.DEFAULT_DISTANCE, "", "")))
//        .flatMap { it?.asIterable() ?: listOf() }
//        .sortedBy { it.distance }
//    }
//
//    activity?.runOnUiThread {
//      adapter?.notifyDataSetChanged()
//    }
//  }
//
//  inner class UserCurrentStateRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
//      = if (viewType == TYPE_USER) ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_promise_detail_user, parent, false))
//    else DividerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_arrival_divider, parent, false))
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
//      =  if (itemList[position].type == TYPE_USER) (holder as ViewHolder).bind(itemList[position])
//    else (holder as DividerViewHolder).bind(itemList[position])
//
//    override fun getItemCount(): Int = itemList.size
//
//    override fun getItemViewType(position: Int): Int {
//      return itemList[position].type
//    }
//
//    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//      fun bind(item: Item) {
//
//        GlideApp.with(itemView).load(item.profileImagePath).fitCenter().placeholder(R.drawable.image_place_holder).into(itemView.profileImageView)
//        itemView.nicknameTextView.text = item.nickname
//
//        itemView.notifyPromiseButton.setOnClickListener {
//          MaterialDialog.Builder(itemView.context)
//            .title("알림")
//            .content("알림 메세지를 입력하세요")
//            .positiveText(R.string.confirm)
//            .negativeText(R.string.cancel)
//            .inputRangeRes(2, 80, R.color.dark_sky_blue)
//            .input(null, null) { dialog, input ->
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
//            }.show()
//        }
//
//        val contactItem = Realm.getDefaultInstance().where(ContactItem::class.java).equalTo("phone", item.phone).findFirst()
//        contactItem?.let {
//          itemView.nicknameTextView.text = it.nickname
//        }
//
//        if (item.distance >= 100000f) {
//          itemView.dueTimeTextView.text = "행방불명"
//          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.warm_pink))
//          itemView.expectedTimeTextView.text = "GPS 차단 중"
//        } else if (item.distance <= Define.DEFAULT_DISTANCE) {
//          itemView.dueTimeTextView.text = "도착"
//          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.warm_pink))
//          itemView.expectedTimeTextView.text = String.format("약 %.2f km", item.distance / 1000f)
//        } else {
//          itemView.dueTimeTextView.text = "이동 중"
//          itemView.dueTimeTextView.setTextColor(resources.getColor(R.color.periwinkle_blue))
//          itemView.expectedTimeTextView.text = String.format("약 %.2f km", item.distance / 1000f)
//        }
//      }
//    }
//
//    inner class DividerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//      fun bind(item: Item) {
//        itemView.promisedTimeTextView.text = item.nickname
//      }
//    }
//  }

  data class Item(val type: Int, val phone: String, val nickname: String, val distance: Float, val profileImagePath: String, val pushToken: String)

  companion object {
    val TYPE_USER = 0
    val TYPE_DEADLINE = 1
  }
}