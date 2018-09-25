package blackburn.io.catchup.ui

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import android.util.Log
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import blackburn.io.catchup.ui.creation.NewPromiseActivity
import com.amazonaws.amplify.generated.graphql.CheckAppVersionQuery
import com.amazonaws.amplify.generated.graphql.ListCatchUpUserQuery
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity: BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]

    addPromiseButton.setOnClickListener {
      val intent = Intent(this, NewPromiseActivity::class.java)
      intent.putExtra("phone", intent.getStringExtra("phone"))
      startActivityForResult(intent, 100)
    }
  }
}
