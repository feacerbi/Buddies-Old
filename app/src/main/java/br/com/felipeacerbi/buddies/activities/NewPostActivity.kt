package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.BuddyInfo
import br.com.felipeacerbi.buddies.models.Post
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_new_post.*

class NewPostActivity : FireListener() {

    companion object {
        val TAG = "NewPostActivity"
        val RC_PHOTO_PICKER = 100
        val PLACE_PICKER_REQUEST = 101
        val BUDDY_KEY_EXTRA = "buddy_key"
        val BUDDY_INFO_EXTRA = "buddy_info"
        val SHARE_POST_ID = "share_id"
    }

    var buddyKey: String? = null
    var buddyInfo: BuddyInfo? = null
    var shareId: String? = null

    var photoUrl: String = ""
    var mapsPlace: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        handleIntent(intent)
        setUpUI()
    }

    private fun handleIntent(intent: Intent?) {
        if(intent != null && intent.hasExtra(BUDDY_KEY_EXTRA)) {
            buddyKey = intent.getStringExtra(BUDDY_KEY_EXTRA)
            buddyInfo = intent.getSerializableExtra(BUDDY_INFO_EXTRA) as BuddyInfo
        } else {
            shareId = intent?.getStringExtra(SHARE_POST_ID)
        }
    }

    private fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        if(shareId != null) {
            supportActionBar?.title = "Sharing"
        }

        poster_name.text = buddyInfo?.name
        post_post_button.isEnabled = false
        post_location.visibility = View.GONE

        val info = buddyInfo
        if(info != null && info.photo.isNotEmpty()) {
            Picasso.with(this)
                    .load(info.photo)
                    .error(R.drawable.no_phototn)
                    .placeholder(R.drawable.no_phototn)
                    .fit()
                    .centerCrop()
                    .into(post_profile_photo)
        }

        post_message.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                // Nothing
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Nothing
            }

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                post_post_button.isEnabled = count != 0
            }
        })

        if(shareId == null) {
            post_add_image.visibility = View.VISIBLE
            post_add_image.setOnClickListener { pickImage() }
        } else {
            post_add_image.visibility = View.GONE

        }

        post_add_location.setOnClickListener { pickPlace() }
        post_add_mark.setOnClickListener {  }

        post_post_button.setOnClickListener { createNewPost() }
    }

    private fun pickPlace() {
        val builder = PlacePicker.IntentBuilder()
        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/"
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER)
    }

    private fun createNewPost() {
        val id = buddyKey

        if (id != null) {
            val newPost = Post(
                    id,
                    post_message.text.toString(),
                    photoUrl,
                    mapsPlace?.name.toString()
            )

            firebaseService.addPost(newPost)
            Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity request code " + requestCode)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    photoUrl = data?.data.toString()
                    Log.d(TAG, "Image picked")
                    post_photo.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, data?.data))
                }
                PLACE_PICKER_REQUEST -> {
                    mapsPlace = PlacePicker.getPlace(this, data)
                    post_location.text = mapsPlace?.name
                    post_location.visibility = View.VISIBLE
                }
            }
        }
    }

}