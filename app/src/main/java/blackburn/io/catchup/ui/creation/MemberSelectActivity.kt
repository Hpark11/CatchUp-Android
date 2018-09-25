package blackburn.io.catchup.ui.creation

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import blackburn.io.catchup.R
import blackburn.io.catchup.app.BaseActivity
import javax.inject.Inject

class MemberSelectActivity: BaseActivity() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory

  private lateinit var viewModel: MemberSelectViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_member_select)

    viewModel = ViewModelProviders.of(this, viewModelFactory)[MemberSelectViewModel::class.java]
  }
}
