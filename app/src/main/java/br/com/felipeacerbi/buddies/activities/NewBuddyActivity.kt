package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.util.Log
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.showListDialog
import kotlinx.android.synthetic.main.activity_new_pet.*
import org.parceler.Parcels

class NewBuddyActivity : FireListener() {

    companion object {
        val TAG = "NewBuddyActivity"
        val BUDDY_EXTRA = "buddy"
        val EXTRA_BASETAG = "basetag"
        val EXTRA_TAG_KEY = "tag_key"
        val RC_PHOTO_PICKER = 1
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    var tagKey = ""
    var baseTag: BaseTag? = null
    var photoUrl = ""
    var petSelected = 0
    var breedSelected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_pet)

        handleIntent(intent)
        setUpUI()
    }

    fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        pet_chooser.isEnabled = false
        breed_chooser.isEnabled = false

        initPetChooser()

        picture_edit_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
        }

        cancel_button.setOnClickListener {
            onBackPressed()
        }

        add_button.setOnClickListener {
            val name = pet_name.text.toString()
            val pet = pet_chooser.text.toString()
            val breed = breed_chooser.text.toString()

            val resultIntent = Intent(this, MainActivity::class.java)
            resultIntent.putExtra(BUDDY_EXTRA, Parcels.wrap(Buddy(name, pet, breed, photoUrl)))
            resultIntent.putExtra(EXTRA_TAG_KEY, tagKey)
            resultIntent.putExtra(EXTRA_BASETAG, Parcels.wrap(baseTag))

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    fun initPetChooser() {
        fireBuilder.onRef(firebaseService.getAnimalsReference())
                .mode(MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        val items = it.children.map { it.key }.toTypedArray()

                        pet_chooser.text = items[petSelected]
                        initBreedChooser()

                        pet_chooser.setOnClickListener {
                            AlertDialog.Builder(this).showListDialog(
                                    "Animal",
                                    items,
                                    petSelected,
                                    { dialog, position ->
                                        petSelected = position
                                        pet_chooser.text = items[position]

                                        dialog.dismiss()
                                        initBreedChooser()
                                    })
                        }
                        pet_chooser.isEnabled = true
                    }
                }
                .cancel { Log.d(TAG, "Animals not found") }
                .listen()
    }

    fun initBreedChooser() {
        fireBuilder.onRef(firebaseService.getAnimalBreedsReference(pet_chooser.text.toString()))
                .mode(MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        val items = it.children.map { it.key }.toTypedArray()

                        breed_chooser.text = items[breedSelected]
                        breed_chooser.setOnClickListener {
                            AlertDialog.Builder(this).showListDialog(
                                    "Breed",
                                    items,
                                    breedSelected,
                                    { dialog, position ->
                                        breedSelected = position
                                        breed_chooser.text = items[position]

                                        dialog.dismiss()
                                    })
                        }
                        breed_chooser.isEnabled = true
                    }
                }
                .cancel { Log.d(TAG, "Breeds not found") }
                .listen()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    photoUrl = data?.data.toString()
                    picture.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, data?.data))
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            tagKey = intent.extras.getString(EXTRA_TAG_KEY)
            baseTag = intent.extras.getSerializable(EXTRA_BASETAG) as BaseTag
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }
}
