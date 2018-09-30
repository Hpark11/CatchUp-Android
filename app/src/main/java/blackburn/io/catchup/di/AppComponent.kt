package blackburn.io.catchup.di

import blackburn.io.catchup.app.App
import blackburn.io.catchup.di.module.ActivityBuilderModule
import blackburn.io.catchup.di.module.AppModule
import blackburn.io.catchup.di.module.ServiceBuilderModule
import blackburn.io.catchup.di.module.ViewModelModule
import blackburn.io.catchup.di.scope.AppScope
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@AppScope
@Component(modules = [
  AndroidSupportInjectionModule::class,
  AppModule::class,
  ViewModelModule::class,
  ActivityBuilderModule::class, ServiceBuilderModule::class
])
interface AppComponent: AndroidInjector<App> {

  @Component.Builder
  abstract class Builder: AndroidInjector.Builder<App>()
}