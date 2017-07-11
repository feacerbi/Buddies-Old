package br.com.felipeacerbi.buddies.activities

import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import br.com.felipeacerbi.buddies.BuddiesApplication
import br.com.felipeacerbi.buddies.FirebaseService
import br.com.felipeacerbi.buddies.NFCService
import br.com.felipeacerbi.buddies.adapters.interfaces.IOnListFragmentInteractionListener
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.fragments.BuddiesListFragment
import br.com.felipeacerbi.buddies.fragments.FollowListFragment
import br.com.felipeacerbi.buddies.models.BaseTag
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.BuddyInfo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.buddies_list.*

class MainActivity : AppCompatActivity(), IOnListFragmentInteractionListener {

    companion object {
        val TAG = "MainActivity"
        val NEW_PET_RESULT = 100
    }

    val firebaseService = FirebaseService()

    val nfcService: NFCService by lazy {
        NFCService()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                transactToFragment(FollowListFragment(), R.id.container)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
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

        handleIntent(intent)
    }

    override fun onStart() {
        super.onStart()

        supportActionBar?.title = firebaseService.getCurrentUserDisplayName()

        firebaseService.registerUser()

        transactToFragment(FollowListFragment(), R.id.container)
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
                    showTwoChoiceCancelableDialog(
                            "TAG Scan",
                            "A new Buddy Tag was detected, what do you want to do?",
                            "Follow Buddy",
                            "Add new Buddy",
                            { _, _ -> firebaseService.addFollowPet(nfcTag)},
                            { _, _ -> launchNewPetActivity(nfcTag) })
                }
            }

        } else {
            Log.d(TAG, "Intent null")
        }
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

    override fun onListFragmentInteraction() {
        progress.visibility = View.INVISIBLE
        container.visibility = View.VISIBLE
    }

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
