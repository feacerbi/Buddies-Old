package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.base.TagHandlerActivity
import br.com.felipeacerbi.buddies.fragments.FirebaseListFragment
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.makeQueryBundle
import br.com.felipeacerbi.buddies.utils.showOneChoiceCancelableDialog
import br.com.felipeacerbi.buddies.utils.transact
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : TagHandlerActivity() {

    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpUI()

        firebaseService.registerUser()

        handleIntent(intent, true)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
//                transactToFragment(
//                        FirebaseListFragment(),
//                        container.id,
//                        makeQueryBundle(firebaseService.queryFollow()))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_following -> {
                FirebaseListFragment().transact(
                        this,
                        container.id,
                        Bundle().makeQueryBundle(this, firebaseService.queryFollow())
                )
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_places -> {
//                transactToFragment(
//                        RequestListFragment(),
//                        container.id,
//                        makeQueryBundle(firebaseService.queryRequests()))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun setUpUI() {
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportActionBar?.title = resources.getString(R.string.app_name)
        setUpFab(fab)

        FirebaseListFragment().transact(
                this,
                container.id,
                Bundle().makeQueryBundle(this, firebaseService.queryFollow())
        )
    }

    override fun showTagOptionsDialog(baseTag: BaseTag) {
        AlertDialog.Builder(this).showOneChoiceCancelableDialog(
                getString(R.string.tag_options_dialog_title),
                getString(R.string.tag_options_dialog_message),
                getString(R.string.tag_options_dialog_follow_button),
                { _, _ -> addNewFollow(baseTag) }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity result code " + requestCode)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_sign_out -> signOut()
            R.id.action_settings -> launchActivity(SettingsActivity::class)
            R.id.action_profile -> launchActivity(ProfileActivity::class)
            R.id.action_requests -> launchActivity(RequestsActivity::class)
        }

        return super.onOptionsItemSelected(item)
    }

    fun signOut() {
        firebaseService.signOut()
        launchLoginActivity()
    }

    private fun launchLoginActivity() {
        launchActivity(LoginActivity::class)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.fragments?.forEach {
            if(it is FirebaseListFragment) {
                it.cleanUp()
            }
        }
    }
}
