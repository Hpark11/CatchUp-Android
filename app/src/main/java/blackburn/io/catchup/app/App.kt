package blackburn.io.catchup.app

import android.content.Context
import blackburn.io.catchup.di.DaggerAppComponent
import com.kakao.auth.KakaoSDK
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App: DaggerApplication(), HasActivityInjector {

  @Inject
  lateinit var sdkAdapter: KakaoSDKAdapter

  override fun onCreate() {
    super.onCreate()
    KakaoSDK.init(sdkAdapter)
  }

  override fun applicationInjector(): AndroidInjector<out DaggerApplication>
    = DaggerAppComponent.builder().create(this)

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
  }
}