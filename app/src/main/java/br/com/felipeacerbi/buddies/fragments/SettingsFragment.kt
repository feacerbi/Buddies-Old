package br.com.felipeacerbi.buddies.fragments


import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v4.app.Fragment

import br.com.felipeacerbi.buddies.R


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)
    }

}
