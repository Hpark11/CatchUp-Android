package blackburn.io.catchup.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import blackburn.io.catchup.di.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

//  @Binds
//  @IntoMap
//  @ViewModelKey(SplashScreenViewModel::class)
//  abstract fun bindSplashScreenViewModel(viewModel: SplashScreenViewModel): ViewModel

  @Binds
  abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}