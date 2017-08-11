package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.getFirebaseAdapter
import br.com.felipeacerbi.buddies.utils.showInputDialog
import br.com.felipeacerbi.buddies.utils.showListDialog
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pet_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*

class BuddyProfileActivity : FireListener() {

    companion object {
        val TAG = "BuddyProfileActivity"
        val EXTRA_PETID = "extra_petid"
        val EXTRA_EDITABLE = "extra_editable"
        val RC_PHOTO_PICKER = 1
    }

    val editViewList by lazy {
        arrayOf(buddy_name_edit_button, picture_edit_button)
    }

    var buddy: Buddy? = null
    var petId = ""
    var petSelected = 0
    var breedSelected = 0

    var buddyReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

        handleIntent(intent)
    }

    fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        with(posts_list) {
            layoutManager = LinearLayoutManager(context)
//            adapter =
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

    override fun onResume() {
        super.onResume()
        setUpUI()

        FireBuilder().onRef(buddyReference)
                .mode(MODE_CONTINUOUS)
                .complete {
                    if(it != null && it.hasChildren()) {
                        Log.d(TAG, it.key)
                        buddy = Buddy(it)
                        buddy_name.text = buddy?.name
                        actionBar?.title = buddy?.name
                        actionBar?.subtitle = buddy?.followers?.size.toString() + " followers"
                        pet_chooser.text = buddy?.animal
                        breed_chooser.text = buddy?.breed

                        val buddyPhoto = buddy?.photo
                        if(buddyPhoto != null && buddyPhoto.isNotEmpty()) {
                            Picasso.with(this)
                                    .load(buddyPhoto)
                                    .error(R.drawable.no_phototn)
                                    .placeholder(R.drawable.no_phototn)
                                    .fit()
                                    .centerCrop()
                                    .into(picture)
                        }

                        setEditables(intent.extras.getBoolean(EXTRA_EDITABLE))
                    }
                }
                .cancel { Log.d(TAG, "Buddy not found") }
                .listen()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity request code " + requestCode)

        if(resultCode == RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    val path = data?.data
                    if(path != null) {
                        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
                        firebaseService.uploadPetFile(petId, path) {
                            downloadUrl ->
                            buddy?.photo = downloadUrl.toString()
                            updateBuddy()
                        }
                    }
                }
            }
        }
    }

    fun initPetChooser() {
        pet_chooser.setTypeface(null, Typeface.BOLD)
        FireBuilder().onRef(firebaseService.getAnimalsReference())
                .mode(MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        val items = it.children.map { it.key }.toTypedArray()

                        petSelected = items.indexOf(buddy?.animal)
                        pet_chooser.setOnClickListener {
                            AlertDialog.Builder(this).showListDialog(
                                    "Animal",
                                    items,
                                    petSelected,
                                    { dialog, position ->
                                        pet_chooser.text = items[position]
                                        buddy?.animal = items[position]
                                        updateBuddy()

                                        dialog.dismiss()
                                        initBreedChooser(true)
                                    })
                        }
                    }
                }
                .cancel { Log.d(TAG, "Animals not found") }
                .listen()
    }

    fun initBreedChooser(show: Boolean = false) {
        breed_chooser.setTypeface(null, Typeface.BOLD)
        FireBuilder().onRef(firebaseService.getAnimalBreedsReference(pet_chooser.text.toString()))
                .mode(MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        val items = it.children.map { it.key }.toTypedArray()

                        breedSelected = items.indexOf(buddy?.breed)
                        if(show) {
                            breed_chooser.text = items[breedSelected]
                            buddy?.breed = items[breedSelected]
                            updateBuddy()
                            showBreedDialog(items)
                        }

                        breed_chooser.setOnClickListener {
                            showBreedDialog(items)
                        }
                    }
                }
                .cancel { Log.d(TAG, "Breeds not found") }
                .listen()
    }

    fun showBreedDialog(items: Array<String>) {
        AlertDialog.Builder(this).showListDialog(
                "Breed",
                items,
                breedSelected,
                { dialog, position ->
                    breedSelected = position
                    breed_chooser.text = items[position]
                    buddy?.breed = items[position]
                    updateBuddy()

                    dialog.dismiss()
                })
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            petId = intent.extras.getString(EXTRA_PETID)
            buddyReference = firebaseService.getPetReference(petId)
        }
    }

    fun setEditables(isEditable: Boolean) {
        Log.d(TAG, "Editing...")
        for(view in editViewList) {
            view.visibility = if(isEditable) View.VISIBLE else View.GONE
        }

        if(isEditable) {
            initPetChooser()
            initBreedChooser()

            buddy_name_edit_button.setOnClickListener {
                showEditDialog("Edit name", buddy_name.text.toString(), { buddy?.name = it })
            }

            picture_edit_button.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
            }
        }
    }

    fun updateBuddy() = firebaseService.updatePet(buddy, petId)

    override fun onDestroy() {
        super.onDestroy()
        posts_list.getFirebaseAdapter()?.cleanup()
    }
}