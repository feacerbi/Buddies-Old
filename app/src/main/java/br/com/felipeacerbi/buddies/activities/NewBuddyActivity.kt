package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.models.BuddyInfo
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import kotlinx.android.synthetic.main.activity_new_pet.*

class NewBuddyActivity : AppCompatActivity() {

    companion object {
        val BUDDY_INFO_EXTRA = "buddy_info"
        val EXTRA_BASETAG = "basetag"
        val RC_PHOTO_PICKER = 1
    }

    var baseTag: BaseTag? = null
    var photoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_pet)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleIntent(intent)

        picture_edit_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
        }

        cancel_button.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        add_button.setOnClickListener {
            val name = pet_name.text.toString()
            val breed = breed.text.toString()

            val resultIntent = Intent(this, MainActivity::class.java)
            resultIntent.putExtra(BUDDY_INFO_EXTRA, BuddyInfo(name, breed, photoUrl))
            resultIntent.putExtra(EXTRA_BASETAG, baseTag)

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    photoUrl = data.data.toString()
                    picture.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, data.data))
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            baseTag = intent.extras.getSerializable(EXTRA_BASETAG) as BaseTag
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }
}
