package blackburn.io.catchup.di.module

import android.content.Context
import blackburn.io.catchup.di.scope.AppScope
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import dagger.Module
import dagger.Provides

@Module(includes = [])
class APIClientModule {
  @Provides
  @AppScope
  fun provideAPIClient(context: Context): AWSAppSyncClient
    = AWSAppSyncClient.builder().context(context).awsConfiguration(AWSConfiguration(context)).build()
}