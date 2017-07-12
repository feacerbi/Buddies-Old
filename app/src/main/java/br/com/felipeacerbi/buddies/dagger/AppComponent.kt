package br.com.felipeacerbi.buddies.dagger

import br.com.felipeacerbi.buddies.activities.MainActivity
import br.com.felipeacerbi.buddies.fragments.FirebaseListFragment
import dagger.Component
import javax.inject.Singleton

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
@Singleton
@Component(modules = arrayOf(AppModule::class))

interface AppComponent {
    fun inject(mainActivity: MainActivity)
    fun inject(fragment: FirebaseListFragment)
}