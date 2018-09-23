package blackburn.io.catchup.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import blackburn.io.catchup.di.viewmodel.ViewModelFactory
import blackburn.io.catchup.di.viewmodel.ViewModelKey
import blackburn.io.catchup.ui.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindSplashScreenViewModel(viewModel: MainViewModel): ViewModel

  @Binds
  abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}