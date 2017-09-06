package br.com.felipeacerbi.buddies.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.FullscreenPhotoActivity
import br.com.felipeacerbi.buddies.activities.QRCodeActivity
import br.com.felipeacerbi.buddies.activities.SettingsActivity
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pet_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*
import kotlinx.android.synthetic.main.pet_profile_info_fragment.*

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class PetProfileInfoFragment : Fragment() {

    val firebaseService = FirebaseService()

    companion object {
        val TAG = "PetProfileInfoFragment"
        val RC_PHOTO_PICKER = 1
    }

    val permissionsManager: PermissionsManager by lazy {
        PermissionsManager(activity)
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    val editViewList by lazy {
        arrayOf(buddy_name_edit_button, picture_edit_button, buddy_animal_edit_button, buddy_breed_edit_button, tag_info_button)
    }

    val buddyReference by lazy {
        firebaseService.getDatabaseReference(arguments.getString(PetsListFragment.DATABASE_REFERENCE))
    }

    var buddy: Buddy? = null
    var petId = ""
    var petSelected = 0
    var breedSelected = 0

    var editable = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.pet_profile_info_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    fun setUpViews() {
        for(editView in editViewList) {
            editView?.visibility = if(editable) View.VISIBLE else View.INVISIBLE
        }

        if(editable) {
            initPetChooser()
            initBreedChooser()
            displayTagValue()

            buddy_name_edit_button.setOnClickListener {
                showEditDialog("Edit name", buddy_name.text.toString(), { buddy?.name = it })
            }

            picture_edit_button.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
            }

            tag_info_button.setOnClickListener {
                AlertDialog.Builder(context).showTextDialog("Change Tag", "Please approach an NFC TAG if your device supports or enable the camera button on Settings to scan for the TAG QR Code.")
            }
        }
    }

    fun displayTagValue() {
        val tagId = buddy?.tagId

        if(tagId != null) {
            val fireBuilder = (activity as FireListener).FireBuilder()
            fireBuilder.onRef(firebaseService.getTagReference(tagId))
                    .mode(FireListener.MODE_SINGLE)
                    .complete {
                        if(it != null && it.hasChildren()) {
                            val tag = BaseTag(it)
                            tag_value.text = tag.id
                        }
                    }
                    .cancel { Log.d(TAG, "Tag not found") }
                    .listen()
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
                        updateBuddy()
                    }
            )
        }
    }

    fun initPetChooser() {
        val fireBuilder = (activity as FireListener).FireBuilder()
        fireBuilder.onRef(firebaseService.getAnimalsReference())
                .mode(FireListener.MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        val items = it.children.map { it.key }.toTypedArray()
                        setPetChooser(items)
                    }
                }
                .cancel { Log.d(TAG, "Animals not found") }
                .listen()
    }

    fun initBreedChooser(show: Boolean = false) {
        val fireBuilder = (activity as FireListener).FireBuilder()
        fireBuilder.onRef(firebaseService.getAnimalBreedsReference(pet_chooser.text.toString()))
                .mode(FireListener.MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        val items = it.children.map { it.key }.toTypedArray()
                        setBreedChooser(items, show)
                    }
                }
                .cancel { Log.d(TAG, "Breeds not found") }
                .listen()
    }

    fun setPetChooser(items: Array<String>) {
        petSelected = items.indexOf(buddy?.animal)
        buddy_animal_edit_button.setOnClickListener {
            showPetDialog(items)
        }
    }

    fun setBreedChooser(items: Array<String>, show: Boolean) {
        if(show) {
            selectBreed(items, 0)
            showBreedDialog(items)
        } else {
            breedSelected = items.indexOf(buddy?.breed)
        }

        buddy_breed_edit_button.setOnClickListener {
            showBreedDialog(items)
        }
    }

    fun showPetDialog(items: Array<String>) {
        AlertDialog.Builder(activity).showListDialog(
                "Animal",
                items,
                petSelected,
                { dialog, position ->
                    selectPet(items, position)
                    dialog.dismiss()
                    initBreedChooser(true)
                })
    }

    fun showBreedDialog(items: Array<String>) {
        AlertDialog.Builder(activity).showListDialog(
                "Breed",
                items,
                breedSelected,
                { dialog, position ->
                    selectBreed(items, position)
                    dialog.dismiss()
                })
    }

    fun selectPet(items: Array<String>, position: Int) {
        petSelected = position
        pet_chooser.text = items[position]
        buddy?.animal = items[position]
        updateBuddy()
    }

    fun selectBreed(items: Array<String>, position: Int) {
        breedSelected = position
        breed_chooser.text = items[position]
        buddy?.breed = items[position]
        updateBuddy()
    }

    fun updateBuddy() = firebaseService.updatePet(buddy, petId)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "Activity request code " + requestCode)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    val path = data?.data
                    if(path != null) {
                        Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show()
                        firebaseService.uploadPetFile(petId, path) {
                            downloadUrl ->
                            buddy?.photo = downloadUrl.toString()
                            updateBuddy()
                        }
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        setUpFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))

        val fireBuilder = (activity as FireListener).FireBuilder()
        fireBuilder.onRef(buddyReference)
                .mode(FireListener.MODE_CONTINUOUS)
                .complete {
                    if(it != null && it.hasChildren()) {
                        buddy = Buddy(it)
                        buddy_name.text = buddy?.name
                        pet_chooser.text = buddy?.animal
                        breed_chooser.text = buddy?.breed
                        tag_value.text = buddy?.tagId

                        val isOwner = buddy?.owners?.containsKey(firebaseService.getCurrentUserUID()) ?: false
                        if(isOwner) {
                            editable = true
                        }

                        setUpViews()

                        val buddyPhoto = buddy?.photo
                        if(buddyPhoto != null && buddyPhoto.isNotEmpty()) {
                            Picasso.with(activity)
                                    .load(buddyPhoto)
                                    .error(R.drawable.no_phototn)
                                    .placeholder(R.drawable.no_phototn)
                                    .fit()
                                    .centerCrop()
                                    .into(picture)

                            picture.setOnClickListener {
                                launchActivityWithExtras<FullscreenPhotoActivity>(
                                        FullscreenPhotoActivity::class,
                                        arrayOf(FullscreenPhotoActivity.PHOTO_PATH,
                                                FullscreenPhotoActivity.TOOLBAR_TITLE),
                                        arrayOf(buddyPhoto, buddy?.name ?: ""))
                            }
                        }
                    }
                }
                .cancel { Log.d(TAG, "Buddy not found") }
                .listen()
    }

    fun setUpFab(show: Boolean) {
        activity.fab?.setUp(activity, show, R.drawable.ic_add_a_photo_white_24dp) {
            permissionsManager.actionWithPermission(Manifest.permission.CAMERA) { activity.launchActivity(QRCodeActivity::class) }
        }
    }
}