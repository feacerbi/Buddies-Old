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
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.getFirebaseAdapter
import br.com.felipeacerbi.buddies.utils.setUp
import br.com.felipeacerbi.buddies.utils.showOneChoiceCancelableDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*

class ProfileActivity : TagHandlerActivity() {

    companion object {
        val TAG = "ProfileActivity"
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setUpUI()
    }

    fun setUpUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setUpFab(fab)

        profile_name.text = firebaseService.getCurrentUserDisplayName()
        profile_email.text = firebaseService.getCurrentUserEmail()

        Picasso.with(this)
                .load(firebaseService.getCurrentUserPicture())
                .error(R.mipmap.ic_launcher)
                .resize(400, 400)
                .centerCrop()
                .into(profile_picture)

        // Set the adapter
        with(buddies_list) {
            layoutManager = LinearLayoutManager(context)
            adapter = BuddiesAdapter(firebaseService.queryBuddies(), progress)
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
        setUpFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))
    }

    fun setUpFab(show: Boolean) {
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
