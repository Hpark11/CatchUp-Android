package blackburn.io.catchup.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import blackburn.io.catchup.di.viewmodel.ViewModelFactory
import blackburn.io.catchup.di.viewmodel.ViewModelKey
import blackburn.io.catchup.ui.MainViewModel
import blackburn.io.catchup.ui.creation.MemberSelectViewModel
import blackburn.io.catchup.ui.creation.NewPromiseViewModel
import blackburn.io.catchup.ui.detail.PromiseDetailMembersViewModel
import blackburn.io.catchup.ui.detail.PromiseDetailViewModel
import blackburn.io.catchup.ui.entrance.EntranceViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(EntranceViewModel::class)
  abstract fun bindEntranceViewModel(viewModel: EntranceViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MemberSelectViewModel::class)
  abstract fun bindMemberSelectViewModel(viewModel: MemberSelectViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(NewPromiseViewModel::class)
  abstract fun bindNewPromiseViewModel(viewModel: NewPromiseViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(PromiseDetailViewModel::class)
  abstract fun bindPromiseDetailViewModel(viewModel: PromiseDetailViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(PromiseDetailMembersViewModel::class)
  abstract fun bindPromiseDetailMembersViewModel(viewModel: PromiseDetailMembersViewModel): ViewModel

  @Binds
  abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}