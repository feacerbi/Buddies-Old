package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.fragments.FirebaseListFragment
import br.com.felipeacerbi.buddies.fragments.RequestListFragment
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.BuddyInfo
import br.com.felipeacerbi.buddies.nfc.NFCService
import br.com.felipeacerbi.buddies.nfc.tags.BaseTag
import br.com.felipeacerbi.buddies.utils.showTwoChoiceCancelableDialog
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : RxBaseActivity() {

    companion object {
        val TAG = "MainActivity"
        val NEW_PET_RESULT = 100
        val QR_CODE_RESULT = 101
    }

    val firebaseService = FirebaseService()

    val nfcService: NFCService by lazy {
        NFCService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpUI()

        firebaseService.registerUser()

        transactToFragment(
                FirebaseListFragment(),
                container.id,
                makeQueryBundle(firebaseService.queryFollow()))

        handleIntent(intent)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_following -> {
                transactToFragment(
                        FirebaseListFragment(),
                        container.id,
                        makeQueryBundle(firebaseService.queryFollow()))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_buddies -> {
                transactToFragment(
                        FirebaseListFragment(),
                        container.id,
                        makeQueryBundle(firebaseService.queryBuddies()))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                transactToFragment(
                        RequestListFragment(),
                        container.id,
                        makeQueryBundle(firebaseService.queryRequests()))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun transactToFragment(fragment: Fragment, id: Int, bundle: Bundle) {
        val transaction = supportFragmentManager.beginTransaction()
        fragment.arguments = bundle

        transaction.replace(id, fragment)
        transaction.commit()
    }

    fun makeQueryBundle(query: Query): Bundle {
        val bundle = Bundle()
        bundle.putString(FirebaseListFragment.DATABASE_REFERENCE, query.toString().removePrefix(getString(R.string.firebase_query_prefix)))
        return bundle
    }

    fun setUpUI() {
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        supportActionBar?.title = firebaseService.getCurrentUserDisplayName()
        fab.setOnClickListener { launchQRCodeActivity() }
    }

    override fun onResume() {
        super.onResume()
        nfcService.activateForegroundDispatchSystem(this)
    }

    override fun onPause() {
        super.onPause()
        nfcService.deactivateForegroundDispatchSystem(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    fun handleIntent(intent: Intent?) {
        if(intent != null) {
            Log.d(TAG, "Intent action: " + intent.action)

            when(intent.action) {
                NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                    val nfcTag = nfcService.parseIntent(intent)
                    val baseTag = nfcTag.baseTag
                    showTagOptions(baseTag)
                }
            }

        } else {
            Log.d(TAG, "Intent null")
        }
    }

    fun showTagOptions(baseTag: BaseTag) {
        AlertDialog.Builder(this).showTwoChoiceCancelableDialog(
                "TAG Scan",
                "A new Buddy Tag was detected, what do you want to do?",
                "Follow Buddy",
                "Add new Buddy",
                { _, _ -> checkPetWithAction(baseTag,
                        { firebaseService.addFollowPet(it) },
                        { Log.d(TAG, "Follow pet not found") }) },
                { _, _ -> checkPetWithAction(baseTag,
                        { firebaseService.addPetOwnerRequest(it)
                            Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show() },
                        { launchNewPetActivity(it) }) })
    }

    fun checkPetWithAction(baseTag: BaseTag,
                       existsAction: (BaseTag) -> Unit,
                       notExistsAction: (BaseTag) -> Unit) {
        Log.d(TAG, "Check pet with action " + baseTag.id)
        val subscription = firebaseService.checkPetObservable(baseTag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { foundTag ->
                            if(foundTag.petId.isEmpty()) {
                                Log.d(TAG, "Tag not found")
                                notExistsAction(foundTag)
                            } else {
                                Log.d(TAG, "Tag found")
                                existsAction(foundTag)
                            }
                        },
                        { e -> Log.d(TAG, "Error adding pet " + e.message) })
        subscriptions.add(subscription)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity result code " + requestCode)

        if(resultCode == RESULT_OK) {
            when(requestCode) {
                NEW_PET_RESULT -> {
                    val buddyInfo = data.extras.getSerializable(NewPetActivity.BUDDY_INFO_EXTRA) as BuddyInfo
                    val baseTag = data.extras.getSerializable(NewPetActivity.EXTRA_BASETAG) as BaseTag
                    firebaseService.addNewPet(baseTag, Buddy(buddyInfo.name, buddyInfo.breed))
                }
                QR_CODE_RESULT -> { showTagOptions(BaseTag(data.extras.getString(QRCodeActivity.QR_CODE_TEXT))) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_sign_out -> signOut()
            R.id.action_settings -> launchSettingsActivity()
        }

        return super.onOptionsItemSelected(item)
    }

    fun signOut() {
        firebaseService.signOut()
        launchLoginActivity()
    }

    fun launchNewPetActivity(baseTag: BaseTag) {
        val newPetActivityIntent = Intent(this, NewPetActivity::class.java)
        newPetActivityIntent.putExtra(NewPetActivity.EXTRA_BASETAG, baseTag)
        startActivityForResult(newPetActivityIntent, NEW_PET_RESULT)
    }

    fun launchQRCodeActivity() {
        val qrCodeActivityIntent = Intent(this, QRCodeActivity::class.java)
        startActivityForResult(qrCodeActivityIntent, QR_CODE_RESULT)
    }

    private fun launchLoginActivity() {
        val loginActivityIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginActivityIntent)
        finish()
    }

    private fun launchSettingsActivity() {
        val settingsActivityIntent = Intent(this, SettingsActivity::class.java)
        startActivity(settingsActivityIntent)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.fragments?.forEach {
            if(it is FirebaseRecyclerAdapter<*, *>) {
                it.cleanup()
            }
        }
    }
}
