package br.com.felipeacerbi.buddies.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.base.TagHandlerActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesAdapter
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*

class ProfileActivity : TagHandlerActivity(), IListClickListener {

    companion object {
        val TAG = "ProfileActivity"
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    var user: User? = null

    val userReference = firebaseService.getUserReference(firebaseService.getCurrentUserUID())

    val inputView: View by lazy {
        layoutInflater.inflate(R.layout.input_dialog, null)
    }

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
            adapter = BuddiesAdapter(this@ProfileActivity, firebaseService.queryBuddies(), progress)
        }

        profile_name_edit_button.setOnClickListener {
            showEditDialog("Edit name", profile_name.text.toString(), { user?.name = it })
        }

        profile_email_edit_button.setOnClickListener {
            showEditDialog("Edit email", profile_email.text.toString(), { user?.email = it })
        }
    }

    fun showEditDialog(title: String, editValue: String, setFunc: (String) -> Unit) {
        with(inputView) {
            input_field.setText(editValue)

            AlertDialog.Builder(context).showInputDialog(
                    title,
                    "OK",
                    this,
                    { _, _ ->
                        val newValue = input_field.text.toString()
                        setFunc(newValue)
                        firebaseService.updateUser(user)
                    }
            )
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
                                .load(user?.photo)
                                .error(R.mipmap.ic_launcher_round)
                                .resize(500, 500)
                                .centerCrop()
                                .into(profile_picture)
                    }
                }
                .cancel { Log.d(TAG, "User not found") }
                .listen()

    }

    override fun onListClick(identifier: String) {
        launchActivityWithStringExtra(BuddyProfileActivity::class, BuddyProfileActivity.EXTRA_PETID, identifier)
    }

    fun showFab(show: Boolean) {
        fab?.setUp(this, show, R.drawable.ic_camera_alt_white_24dp)
    }

    override fun getContext(): Context {
        return this
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
