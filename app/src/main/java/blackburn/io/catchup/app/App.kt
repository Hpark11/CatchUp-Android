package blackburn.io.catchup.app

import android.app.Service
import android.content.Context
import blackburn.io.catchup.di.DaggerAppComponent
import com.kakao.auth.KakaoSDK
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject
import dagger.android.DispatchingAndroidInjector
import io.realm.Realm


class App: DaggerApplication(), HasActivityInjector, HasServiceInjector {

  @Inject
  lateinit var sdkAdapter: KakaoSDKAdapter

  override fun onCreate() {
    super.onCreate()
    KakaoSDK.init(sdkAdapter)
    Realm.init(this)
  }

  @Inject
  lateinit var serviceInjector: DispatchingAndroidInjector<Service>

  override fun applicationInjector(): AndroidInjector<out DaggerApplication>
    = DaggerAppComponent.builder().create(this)

  override fun serviceInjector(): DispatchingAndroidInjector<Service> {
    return serviceInjector
  }

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
  }
}