package blackburn.io.catchup.di.module

import android.app.Application
import android.content.Context
import blackburn.io.catchup.di.scope.AppScope
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Module(includes = [])
class AppModule {

  @Provides
  @AppScope
  fun provideContext(app: Application): Context = app
}