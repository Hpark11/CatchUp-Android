package blackburn.io.catchup.di.module

import android.content.Context
import blackburn.io.catchup.app.App
import blackburn.io.catchup.di.scope.AppScope
import blackburn.io.catchup.service.app.SchedulerUtil
import blackburn.io.catchup.service.app.SharedPrefService
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.RealmConfiguration
import javax.inject.Named


@Module(includes = [APIClientModule::class, ServiceBuilderModule::class])
class AppModule {

  @Provides
  @AppScope
  fun provideSchedulers() = SchedulerUtil(Schedulers.io(), AndroidSchedulers.mainThread())

  @Provides
  @AppScope
  fun provideSharedPrefService(@Named("applicationContext") context: Context)
    = SharedPrefService(context)

  @Provides
  @Named("applicationContext")
  @AppScope
  fun provideContext(app: App): Context = app.applicationContext

  @Provides
  @Named("realmConfigCatchUp")
  @AppScope
  fun provideCatchUpRealmConfiguration(): RealmConfiguration = RealmConfiguration.Builder().build()
}