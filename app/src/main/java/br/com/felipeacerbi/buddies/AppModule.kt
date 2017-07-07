package br.com.felipeacerbi.buddies

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
@Module
class AppModule(val application: BuddiesApplication) {

    @Provides
    @Singleton
    fun provideContext(): Context = application

}