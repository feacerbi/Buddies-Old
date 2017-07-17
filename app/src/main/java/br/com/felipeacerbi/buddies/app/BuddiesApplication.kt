package br.com.felipeacerbi.buddies.app

import android.app.Application
import br.com.felipeacerbi.buddies.dagger.AppComponent

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
class BuddiesApplication : Application() {

    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

//        appComponent = DaggerAppComponent.builder()
//                .appModule(AppModule(this))
//                .build()
    }
}