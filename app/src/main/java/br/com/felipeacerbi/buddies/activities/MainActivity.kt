package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import br.com.felipeacerbi.buddies.BuildConfig
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.base.TagHandlerActivity
import br.com.felipeacerbi.buddies.fragments.FirebaseListFragment
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : TagHandlerActivity() {

    companion object {
        val TAG = "MainActivity"
        val RC_SIGN_IN = 1
        val CREATE_PROFILE = 2
    }

    val authUI by lazy {
        AuthUI.getInstance()
    }

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firebaseAuthStateListener: FirebaseAuth.AuthStateListener by lazy {
        FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if(user != null) {
                Log.d(TAG, "Signed In")
                onSignIn()
            } else {
                startActivityForResult(authUI
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                arrayListOf(AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                        AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setTheme(R.style.AppTheme_NoActionBar)
                        .build(),
                        RC_SIGN_IN)
            }
        }
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun onSignIn() {
        subscriptions.add(subscriptionsManager.checkUserWithActionSubscription(
                existsAction = { Log.d(TAG, "User exists") },
                notExistsAction = {
                    val user = User(
                            name = firebaseService.getCurrentUserDisplayName(),
                            email = firebaseService.getCurrentUserEmail(),
                            picPath = firebaseService.getCurrentUserPicture().toString())

                    firebaseService.registerUser(user) }
        ))

        setUpUI()
        handleIntent(intent, true)
    }

    fun onSignOut() {
        authUI.signOut(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth.removeAuthStateListener(firebaseAuthStateListener)
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

        if(!sharedPreferences.contains(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY)) {
            sharedPreferences.edit()
                    .putBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, !nfcService.isNFCSupported(this))
                    .apply()
        }
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

        when(requestCode) {
            RC_SIGN_IN -> {
                when(resultCode) {
                    Activity.RESULT_CANCELED -> finish()
                    Activity.RESULT_OK -> Log.d(TAG, "Sign in success")
                }
            }

            CREATE_PROFILE -> {
                when(resultCode) {
                    Activity.RESULT_CANCELED -> onSignOut()
                    Activity.RESULT_OK -> Log.d(TAG, "Profile created")
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_sign_out -> onSignOut()
            R.id.action_settings -> launchActivity(SettingsActivity::class)
            R.id.action_profile -> launchActivity(ProfileActivity::class)
            R.id.action_requests -> launchActivity(RequestsActivity::class)
        }

        return super.onOptionsItemSelected(item)
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
