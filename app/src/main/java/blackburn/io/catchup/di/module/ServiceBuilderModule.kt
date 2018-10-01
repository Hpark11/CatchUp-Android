package blackburn.io.catchup.di.module

import blackburn.io.catchup.di.scope.ServiceScope
import blackburn.io.catchup.service.android.LocationTrackingService
import blackburn.io.catchup.service.android.PushMessageService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

  @ServiceScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindPushTokenService(): PushMessageService

  @ServiceScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindLocationTrackingService(): LocationTrackingService
}