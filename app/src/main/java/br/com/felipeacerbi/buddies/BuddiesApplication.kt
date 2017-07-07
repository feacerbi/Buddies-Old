package br.com.felipeacerbi.buddies

import android.app.Application

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
class BuddiesApplication : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }
}