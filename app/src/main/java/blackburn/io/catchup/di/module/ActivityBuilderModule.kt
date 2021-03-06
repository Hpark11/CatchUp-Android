package blackburn.io.catchup.di.module

import blackburn.io.catchup.di.scope.ActivityScope
import blackburn.io.catchup.ui.entrance.EntranceActivity
import blackburn.io.catchup.ui.MainActivity
import blackburn.io.catchup.ui.creation.MemberSelectActivity
import blackburn.io.catchup.ui.creation.NewPromiseActivity
import blackburn.io.catchup.ui.detail.PromiseDetailActivity
import blackburn.io.catchup.ui.detail.PromiseDetailFragmentProvider
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

  @ActivityScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindNewPromiseActivity(): NewPromiseActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [])
  abstract fun bindMemberSelectActivity(): MemberSelectActivity

  @ActivityScope
  @ContributesAndroidInjector(modules = [PromiseDetailFragmentProvider::class])
  abstract fun bindPromiseDetailActivity(): PromiseDetailActivity
}