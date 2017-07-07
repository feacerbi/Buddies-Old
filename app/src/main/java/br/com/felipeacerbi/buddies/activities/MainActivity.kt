package br.com.felipeacerbi.buddies.activities

import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import br.com.felipeacerbi.buddies.BuddiesApplication
import br.com.felipeacerbi.buddies.FirebaseService
import br.com.felipeacerbi.buddies.NFCService
import br.com.felipeacerbi.buddies.adapters.interfaces.IOnListFragmentInteractionListener
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType
import br.com.felipeacerbi.buddies.fragments.BuddiesListFragment
import br.com.felipeacerbi.buddies.models.NFCTag
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), IOnListFragmentInteractionListener {

    companion object {
        val TAG = "MainActivity"
    }

    @Inject lateinit var firebaseService: FirebaseService

    val nfcService: NFCService by lazy {
        NFCService()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                transactToFragment(BuddiesListFragment(), R.id.container)
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

        BuddiesApplication.appComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()

        supportActionBar?.title = firebaseService.getCurrentUserDisplayName()

        firebaseService.registerUser()

        transactToFragment(BuddiesListFragment(), R.id.container)
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
                    showTwoChoiceDialog(
                            "TAG Scan",
                            "A new TAG was detected, what do you want to do?",
                            "Follow Buddy",
                            "Add new Buddy",
                            { _, _ -> showTagInfo(nfcTag) },
                            { _, _ -> showTagInfo(nfcTag)})
                }
            }

        } else {
            Log.d(TAG, "Intent null")
        }
    }

    fun showTagInfo(nfcTag: NFCTag) {
//        showTextDialog("Tag id: " + nfcTag.id + "\n" +
//                "Tag tech: " + nfcTag.tag + "\n" +
//                "Tag message: " + nfcTag.ndefMessage + "\n" +
//                "Tag decoded: " + nfcTag.decodedPayload)

//        val frag = supportFragmentManager.findFragmentById(R.id.container) as BuddiesListFragment
//        frag.addBuddy(nfcTag.decodedPayload)

        nfcTag.petName = "Rex"
        firebaseService.addNewPet(nfcTag)
    }

    override fun onListFragmentInteraction(item: ViewType) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    fun showTextDialog(text: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Alert")
                .setMessage(text)
                .setPositiveButton("OK") { _, _ ->  }
                .show()
    }

    fun showTwoChoiceDialog(
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
                .setNeutralButton(buttonTwoTitle, funcTwo)
                .show()
    }

    override fun onBackPressed() {
        finish()
    }
}
