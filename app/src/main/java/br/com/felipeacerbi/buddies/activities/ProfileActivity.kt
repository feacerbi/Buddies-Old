package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.base.TagHandlerActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesAdapter
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.getFirebaseAdapter
import br.com.felipeacerbi.buddies.utils.setUp
import br.com.felipeacerbi.buddies.utils.showInputDialog
import br.com.felipeacerbi.buddies.utils.showOneChoiceCancelableDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*

class ProfileActivity : TagHandlerActivity() {

    companion object {
        val TAG = "ProfileActivity"
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    val userReference = firebaseService.getUserReference(firebaseService.getCurrentUserUID())

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setUpUI()
    }

    fun setUpUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpFab(fab)

        // Set the adapter
        with(buddies_list) {
            layoutManager = LinearLayoutManager(context)
            adapter = BuddiesAdapter(firebaseService.queryBuddies(), progress)
        }

        profile_name_edit_button.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.input_dialog, null)
            with(view) {
                input_field.setText(profile_name.text)

                AlertDialog.Builder(context).showInputDialog(
                        "Edit Name",
                        "OK",
                        this,
                        { _, _ ->
                            val newName = input_field.text.toString()
                            user?.name = newName
                            firebaseService.updateUser(user)
                        }
                )
            }
        }

        profile_email_edit_button.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.input_dialog, null)
            with(view) {
                input_field.setText(profile_email.text)

                AlertDialog.Builder(context).showInputDialog(
                        "Edit Email",
                        "OK",
                        this,
                        { _, _ ->
                            val newEmail = input_field.text.toString()
                            user?.email = newEmail
                            firebaseService.updateUser(user)
                        }
                )
            }
        }
    }

    override fun showTagOptionsDialog(baseTag: BaseTag) {
        AlertDialog.Builder(this).showOneChoiceCancelableDialog(
                getString(R.string.tag_options_dialog_title),
                getString(R.string.tag_options_dialog_message),
                getString(R.string.tag_options_dialog_new_button),
                { _, _ -> addNewBuddy(baseTag) }
        )
    }

    override fun onResume() {
        super.onResume()
        showFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))

        fireBuilder.onRef(userReference)
                .mode(MODE_CONTINUOUS)
                .complete {
                    if(it != null && it.hasChildren()) {
                        user = User(it)
                        profile_name.text = user?.name
                        profile_email.text = user?.email

                        Picasso.with(this)
                                .load(user?.picPath) // user.profilePicture
                                .error(R.mipmap.ic_launcher_round)
                                .resize(500, 500)
                                .centerCrop()
                                .into(profile_picture)
                    }
                }
                .cancel { Log.d(TAG, "User not found") }
                .listen()

    }

    fun showFab(show: Boolean) {
        fab?.setUp(this, show, R.drawable.ic_camera_alt_white_24dp)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity result code " + requestCode)
    }

    override fun onDestroy() {
        super.onDestroy()
        buddies_list.getFirebaseAdapter()?.cleanup()
    }

}
