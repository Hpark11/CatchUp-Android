package blackburn.io.catchup.ui.detail

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class PromiseDetailActivity: BaseActivity(), HasSupportFragmentInjector {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  @Inject
  lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

  private lateinit var viewModel: PromiseDetailViewModel

  override fun supportFragmentInjector(): AndroidInjector<Fragment> {
    return fragmentInjector
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_promise_detail)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[PromiseDetailViewModel::class.java]
    bindViewModel()
  }

  private fun bindViewModel() {

  }
}
