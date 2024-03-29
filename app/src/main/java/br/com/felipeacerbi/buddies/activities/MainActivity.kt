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
import br.com.felipeacerbi.buddies.fragments.PetsListFragment
import br.com.felipeacerbi.buddies.fragments.PlacesListFragment
import br.com.felipeacerbi.buddies.fragments.PostsListFragment
import br.com.felipeacerbi.buddies.fragments.ProfileFragment
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.launchActivity
import br.com.felipeacerbi.buddies.utils.makeQueryBundle
import br.com.felipeacerbi.buddies.utils.showOneChoiceCancelableDialog
import br.com.felipeacerbi.buddies.utils.transact
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import icepick.Icepick
import icepick.State
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : TagHandlerActivity() {

    companion object {
        val TAG = "MainActivity"
        val RC_SIGN_IN = 1

        val FRAGMENT_POSTS = R.id.navigation_home
        val FRAGMENT_FOLLOWS = R.id.navigation_following
        val FRAGMENT_PLACES = R.id.navigation_places
        val FRAGMENT_PROFILE = R.id.navigation_profile
    }

    @State @JvmField var currentFragment: Int? = null

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
                        .setTheme(R.style.AppTheme)
                        .build(),
                        RC_SIGN_IN)
            }
        }
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    fun onSignIn() {
        subscriptions.add(subscriptionsManager.checkUserSubscription(
                existsAction = {
                    userFound ->
                    Log.d(TAG, "User exists")
                    firebaseService.updateUser(userFound.second)
                },
                notExistsAction = {
                    val user = User(
                            name = firebaseService.getCurrentUserDisplayName(),
                            email = firebaseService.getCurrentUserEmail(),
                            photo = firebaseService.getCurrentUserPicture().toString())

                    firebaseService.registerUser(user) }
        ))
    }

    fun onSignOut() {
        authUI.signOut(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Icepick.restoreInstanceState(this, savedInstanceState)

        setUpUI()
        handleIntent(intent, true)
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
            FRAGMENT_POSTS -> {
                PostsListFragment().transact(
                        this,
                        container.id,
                        Bundle().makeQueryBundle(firebaseService.queryPosts())
                )
                title = "Home"
                currentFragment = FRAGMENT_POSTS
                return@OnNavigationItemSelectedListener true
            }
            FRAGMENT_FOLLOWS -> {
                PetsListFragment().transact(
                        this,
                        container.id,
                        Bundle().makeQueryBundle(firebaseService.queryFollow())
                )
                title = "Following"
                currentFragment = FRAGMENT_FOLLOWS
                return@OnNavigationItemSelectedListener true
            }
            FRAGMENT_PLACES -> {
                PlacesListFragment().transact(
                        this,
                        container.id,
                        Bundle().makeQueryBundle(firebaseService.queryPlaces())
                )
                title = "Places"
                currentFragment = FRAGMENT_PLACES
                return@OnNavigationItemSelectedListener true
            }
            FRAGMENT_PROFILE -> {
                ProfileFragment().transact(
                        this,
                        container.id,
                        Bundle()
                )
                title = "Profile"
                currentFragment = FRAGMENT_PROFILE
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun setUpUI() {
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if(!sharedPreferences.contains(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY)) {
            sharedPreferences.edit()
                    .putBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, !nfcService.isNFCSupported(this))
                    .apply()
        }

        navigation.selectedItemId = FRAGMENT_POSTS
    }

    override fun showTagOptionsDialog(baseTag: BaseTag) {
        AlertDialog.Builder(this).showOneChoiceCancelableDialog(
                getString(R.string.tag_options_dialog_title),
                getString(R.string.tag_options_dialog_message),
                getString(R.string.tag_options_dialog_follow_button),
                { _, _ -> addNewFollow(baseTag) }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity request code " + requestCode)

        when(requestCode) {
            RC_SIGN_IN -> {
                when(resultCode) {
                    Activity.RESULT_CANCELED -> finish()
                    Activity.RESULT_OK -> {
                        Log.d(TAG, "Sign in success")
                        navigation.selectedItemId = FRAGMENT_POSTS
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_sign_out -> onSignOut()
            R.id.action_settings -> launchActivity(SettingsActivity::class)
            R.id.action_requests -> launchActivity(RequestsActivity::class)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.fragments?.forEach {
            if(it is PetsListFragment) {
                it.cleanUp()
            }
        }
    }

    override fun onBackPressed() {
        if(currentFragment == FRAGMENT_POSTS) {
            super.onBackPressed()
        } else {
            navigation.selectedItemId = FRAGMENT_POSTS
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }
}
