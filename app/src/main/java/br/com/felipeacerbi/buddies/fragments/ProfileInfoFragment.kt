package br.com.felipeacerbi.buddies.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.FullscreenPhotoActivity
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.setUp
import br.com.felipeacerbi.buddies.utils.showInputDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*
import kotlinx.android.synthetic.main.profile_info_fragment.*
import kotlinx.android.synthetic.main.profile_info_fragment.view.*

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class ProfileInfoFragment : Fragment() {

    val firebaseService = FirebaseService()

    companion object {
        val TAG = "ProfileInfoFragment"
        val RC_PHOTO_PICKER = 1
    }

    var user: User? = null
    val userReference = firebaseService.getUserReference(firebaseService.getCurrentUserUID())

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.profile_info_fragment, container, false)

        if(view is ScrollView) {
            with(view) {
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
        }

        return view
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "Activity request code " + requestCode)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    val path = data?.data
                    if(path != null) {
                        Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show()
                        firebaseService.uploadPersonalFile(path) {
                            downloadUrl ->
                            user?.photo = downloadUrl.toString()
                            firebaseService.updateUser(user)
                        }
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        setUpFab(false)

        val fireBuilder = (activity as FireListener).FireBuilder()
        fireBuilder.onRef(userReference)
                .mode(FireListener.MODE_CONTINUOUS)
                .complete {
                    if(it != null && it.hasChildren()) {
                        user = User(it)
                        profile_name.text = user?.name
                        profile_email.text = user?.email

                        val photo = user?.photo
                        if(photo != null && photo.isNotEmpty()) {
                            Picasso.with(activity)
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

    fun setUpFab(show: Boolean) {
        activity.fab?.setUp(activity, show, R.drawable.ic_pets_black_24dp) {}
    }
}