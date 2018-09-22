package blackburn.io.catchup.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import android.util.Log
import blackburn.io.catchup.R
import com.amazonaws.amplify.generated.graphql.ListCatchUpUserQuery
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException

class MainActivity: AppCompatActivity() {

  private var mAWSAppSyncClient: AWSAppSyncClient? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    mAWSAppSyncClient = AWSAppSyncClient.builder()
      .context(applicationContext)
      .awsConfiguration(AWSConfiguration(applicationContext))
      .build()

    query()
  }

  fun query() {
    mAWSAppSyncClient?.query(ListCatchUpUserQuery.builder().count(5).build())
      ?.responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
      ?.enqueue(todosCallback)
  }

  private val todosCallback = object : GraphQLCall.Callback<ListCatchUpUserQuery.Data>() {
    override fun onResponse(response: Response<ListCatchUpUserQuery.Data>) {
      Log.i("Results", response.data()?.listCatchUpUser()?.users()?.toString())
    }

    override fun onFailure(e: ApolloException) {
      Log.e("ERROR", e.toString())
    }
  }
}
