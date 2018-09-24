package blackburn.io.catchup.di.module

import android.content.Context
import blackburn.io.catchup.app.App
import blackburn.io.catchup.di.scope.AppScope
import blackburn.io.catchup.service.app.SchedulerUtil
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Named


@Module(includes = [APIClientModule::class])
class AppModule {

  @Provides
  @AppScope
  fun provideSchedulers() = SchedulerUtil(Schedulers.io(), AndroidSchedulers.mainThread())

  @Provides
  @Named("applicationContext")
  @AppScope
  fun provideContext(app: App): Context = app.applicationContext
}