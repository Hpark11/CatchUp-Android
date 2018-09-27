package blackburn.io.catchup.ui.detail

import blackburn.io.catchup.di.scope.FragmentScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PromiseDetailFragmentProvider {

  @FragmentScope
  @ContributesAndroidInjector(modules = [])
  abstract fun providePromiseDetailMapFragment(): PromiseDetailMapFragment

  @FragmentScope
  @ContributesAndroidInjector(modules = [])
  abstract fun providePromiseDetailMembersFragment(): PromiseDetailMembersFragment
}