package blackburn.io.catchup.di.module

import blackburn.io.catchup.di.scope.ActivityScope
import blackburn.io.catchup.ui.EntranceActivity
import blackburn.io.catchup.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {
  @ActivityScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindMainActivity(): MainActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindEntranceActivity(): EntranceActivity
}