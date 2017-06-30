package br.com.felipeacerbi.buddies

import android.content.DialogInterface
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onStart() {
        super.onStart()

        val currentUser = firebaseAuth.currentUser

        supportActionBar?.title = currentUser?.displayName
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
        firebaseAuth.signOut()
        finish()
    }

    override fun onBackPressed() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Sign out")
                .setMessage("Do you want to sign out?")
                .setPositiveButton("Yes") { dialog, whichButton -> signOut() }
                .setNegativeButton("No") { dialog, whichButton -> /* Nothing */ }
                .show()
    }
}
