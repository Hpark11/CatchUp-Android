package blackburn.io.catchup.app

import android.content.Context
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasActivityInjector

class App: DaggerApplication(), HasActivityInjector {
  override fun onCreate() {
    super.onCreate()
  }

  override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
    return DaggerAppComponent.builder().create(this)
  }

  override fun attachBaseContext(base: Context) {
    super.attachBaseContext(base)
  }
}