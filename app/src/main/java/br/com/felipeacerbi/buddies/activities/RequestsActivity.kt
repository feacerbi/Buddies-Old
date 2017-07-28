package br.com.felipeacerbi.buddies.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.fragments.RequestListFragment
import br.com.felipeacerbi.buddies.utils.launchActivity
import br.com.felipeacerbi.buddies.utils.makeQueryBundle
import br.com.felipeacerbi.buddies.utils.transact
import kotlinx.android.synthetic.main.activity_requests.*

class RequestsActivity : AppCompatActivity() {

    val firebaseService = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        RequestListFragment().transact(
                this,
                container.id,
                Bundle().makeQueryBundle(this, firebaseService.queryRequests())
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_simple_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_settings -> launchActivity(SettingsActivity::class)
        }
        return super.onOptionsItemSelected(item)
    }
}
