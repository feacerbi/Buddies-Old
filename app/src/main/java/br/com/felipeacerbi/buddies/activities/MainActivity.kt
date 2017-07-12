package br.com.felipeacerbi.buddies.activities

import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.nfc.NFCService
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.fragments.BuddiesListFragment
import br.com.felipeacerbi.buddies.fragments.FollowListFragment
import br.com.felipeacerbi.buddies.nfc.tags.BaseTag
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.BuddyInfo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : RxBaseActivity() {

    companion object {
        val TAG = "MainActivity"
        val NEW_PET_RESULT = 100
    }

    val firebaseService = FirebaseService()

    val nfcService: NFCService by lazy {
        NFCService()
    }

    val pendingIntent: PendingIntent by lazy {
        PendingIntent.getActivity(this, 0, Intent(this, AppCompatActivity::class.java), 0)
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_following -> {
                transactToFragment(FollowListFragment(), R.id.container)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_buddies -> {
                transactToFragment(BuddiesListFragment(), R.id.container)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                //message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun transactToFragment(fragment: Fragment, id: Int) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(id, fragment)
        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        firebaseService.registerUser()

        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()

        supportActionBar?.title = firebaseService.getCurrentUserDisplayName()

        transactToFragment(FollowListFragment(), R.id.container)
    }

    override fun onResume() {
        super.onResume()

        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(
                this,
                pendingIntent,
                arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)),
                nfcService.getTechs())
    }

    override fun onPause() {
        super.onPause()

        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this)
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
                    showTwoChoiceCancelableDialog(
                            "TAG Scan",
                            "A new Buddy Tag was detected, what do you want to do?",
                            "Follow Buddy",
                            "Add new Buddy",
                            { _, _ -> checkPetWithAction(baseTag,
                                    { firebaseService.addFollowPet(it) },
                                    { Log.d(TAG, "Follow pet not found") }) },
                            { _, _ -> checkPetWithAction(baseTag,
                                    { firebaseService.addPetOwner(it) },
                                    { launchNewPetActivity(it) }) })
                }
            }

        } else {
            Log.d(TAG, "Intent null")
        }
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

    fun launchNewPetActivity(baseTag: BaseTag) {
        val newPetActivityIntent = Intent(this, NewPetActivity::class.java)
        newPetActivityIntent.putExtra(NewPetActivity.EXTRA_BASETAG, baseTag)
        startActivityForResult(newPetActivityIntent, NEW_PET_RESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity result code " + requestCode)

        if(requestCode == NEW_PET_RESULT && resultCode == NewPetActivity.RESULT_OK) {
            val buddyInfo = data.extras.getSerializable(NewPetActivity.BUDDY_INFO_EXTRA) as BuddyInfo
            val baseTag = data.extras.getSerializable(NewPetActivity.EXTRA_BASETAG) as BaseTag
            firebaseService.addNewPet(baseTag, Buddy(buddyInfo.name, buddyInfo.breed))
        }
    }

//    fun showTagInfo(baseTag: BaseTag) {
//        showTextDialog("Tag id: " + baseTag.id + "\n" +
//                "Tag tech: " + baseTag.tagId + "\n" +
//                "Tag message: " + baseTag.ndefMessage + "\n" +
//                "Tag decoded: " + baseTag.decodedPayload)
//
//        val frag = supportFragmentManager.findFragmentById(R.id.container) as BuddiesListFragment
//        frag.addBuddy(baseTag.decodedPayload)
//
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        if (id == R.id.action_sign_out) {
            signOut()
        }

        return super.onOptionsItemSelected(item)
    }

    fun signOut() {
        firebaseService.signOut()
        launchLoginActivity()
    }

    private fun launchLoginActivity() {
        val mainActivityIntent = Intent(this, LoginActivity::class.java)
        startActivity(mainActivityIntent)
        finish()
    }

    fun showTextDialog(text: String, func: (DialogInterface, Int) -> Unit) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Alert")
                .setMessage(text)
                .setPositiveButton("OK", func)
                .show()
    }

    fun showTwoChoiceCancelableDialog(
            title: String,
            message: String,
            buttonOneTitle: String,
            buttonTwoTitle: String,
            funcOne: (DialogInterface, Int) -> Unit,
            funcTwo: (DialogInterface, Int) -> Unit) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonOneTitle, funcOne)
                .setNegativeButton(buttonTwoTitle, funcTwo)
                .setNeutralButton("Cancel") { _, _ ->  }
                .show()
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
