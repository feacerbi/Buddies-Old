package br.com.felipeacerbi.buddies.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.base.TagHandlerActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesAdapter
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*

class ProfileActivity : TagHandlerActivity(), IListClickListener {

    companion object {
        val TAG = "ProfileActivity"
        val RC_PHOTO_PICKER = 1
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    var user: User? = null

    val userReference = firebaseService.getUserReference(firebaseService.getCurrentUserUID())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
    }

    fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // Set the adapter
        with(buddies_list) {
            layoutManager = LinearLayoutManager(context)
            adapter = BuddiesAdapter(this@ProfileActivity, firebaseService.queryBuddies(), firebaseService.getPetsReference(), progress)
        }

        profile_name_edit_button.setOnClickListener {
            showEditDialog("Edit name", profile_name.text.toString(), { user?.name = it })
        }

        profile_email_edit_button.setOnClickListener {
            showEditDialog("Edit email", profile_email.text.toString(), { user?.email = it })
        }

        profile_picture_edit_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
        }
    }

    fun showEditDialog(title: String, editValue: String, setFunc: (String) -> Unit) {
        val inputView = layoutInflater.inflate(R.layout.input_dialog, null)
        with(inputView) {
            input_field.setText(editValue)

            AlertDialog.Builder(context).showInputDialog(
                    title,
                    "OK",
                    this,
                    { _, _ ->
                        setFunc(input_field.text.toString())
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
        setUpUI()
        showFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))

        fireBuilder.onRef(userReference)
                .mode(MODE_CONTINUOUS)
                .complete {
                    if(it != null && it.hasChildren()) {
                        user = User(it)
                        profile_name.text = user?.name
                        profile_email.text = user?.email

                        val photo = user?.photo
                        if(photo != null && photo.isNotEmpty()) {
                            Picasso.with(this)
                                    .load(photo)
                                    .error(R.drawable.no_phototn)
                                    .placeholder(R.drawable.no_phototn)
                                    .fit()
                                    .centerCrop()
                                    .into(profile_picture)

                            profile_picture.setOnClickListener {
                                launchActivityWithExtras<FullscreenPhotoActivity>(
                                        FullscreenPhotoActivity::class,
                                        arrayOf(FullscreenPhotoActivity.PHOTO_PATH,
                                                FullscreenPhotoActivity.TOOLBAR_TITLE),
                                        arrayOf(photo, user?.name ?: ""))
                            }
                        }
                    }
                }
                .cancel { Log.d(TAG, "User not found") }
                .listen()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_settings -> launchActivity(SettingsActivity::class)
            R.id.action_requests -> launchActivity(RequestsActivity::class)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onListClick(identifiers: Array<Any>?) {
        launchActivityWithExtras(
                BuddyProfileActivity::class,
                arrayOf(BuddyProfileActivity.EXTRA_PETID,
                        BuddyProfileActivity.EXTRA_EDITABLE),
                identifiers)
    }

    fun showFab(show: Boolean) {
        fab?.setUp(this, show, R.drawable.ic_add_a_photo_white_24dp) {
            permissionsManager.actionWithPermission(Manifest.permission.CAMERA) { launchActivity(QRCodeActivity::class) }
        }
    }

    override fun getContext(): Context {
        return this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity request code " + requestCode)

        Log.d(TAG, "Activity result code " + resultCode + " " + Activity.RESULT_OK)
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    val path = data?.data
                    if(path != null) {
                        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
                        firebaseService.uploadPersonalFile(path) {
                            downloadUrl ->
                            user?.photo = downloadUrl.toString()
                            firebaseService.updateUser(user)
                        }
                    }
                }
            }
        }
    }

    override fun getViewInflater() = layoutInflater

    override fun onDestroy() {
        super.onDestroy()
        buddies_list.getFirebaseAdapter()?.cleanup()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateUpTo(parentActivityIntent)
    }
}
