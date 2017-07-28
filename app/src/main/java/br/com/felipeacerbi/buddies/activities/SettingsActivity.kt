package br.com.felipeacerbi.buddies.activities

import android.os.Bundle
import android.preference.PreferenceActivity
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.fragments.SettingsFragment
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        val QR_CODE_BUTTON_SHORTCUT_KEY = "enable_qrcode_scan"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupActionBar()

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(container.id, SettingsFragment())
        transaction.commit()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
