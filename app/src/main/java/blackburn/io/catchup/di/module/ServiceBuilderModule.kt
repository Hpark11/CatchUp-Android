package blackburn.io.catchup.di.module

import blackburn.io.catchup.di.scope.AppScope
import blackburn.io.catchup.di.scope.ServiceScope
import blackburn.io.catchup.service.android.PushTokenService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

  @ServiceScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindPushTokenService(): PushTokenService
}