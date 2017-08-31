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
import br.com.felipeacerbi.buddies.models.PostInfo
import com.google.android.gms.location.places.ui.PlacePicker
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_new_post.*

class NewPostActivity : FireListener() {

    companion object {
        val TAG = "NewPostActivity"
        val RC_PHOTO_PICKER = 100
        val PLACE_PICKER_REQUEST = 101
        val POST_KEY_EXTRA = "post_key"
        val POST_INFO_EXTRA = "post_info"
        val BUDDY_KEY_EXTRA = "buddy_key"
        val BUDDY_INFO_EXTRA = "buddy_info"
    }

    var postKey: String? = null
    var postInfo: Post? = null

    var buddyKey: String? = null
    var buddyInfo: BuddyInfo? = null

    var photoUrl: String = ""
    var mapsPlace: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        handleIntent(intent)
        setUpUI()
    }

    private fun handleIntent(intent: Intent?) {
        if(intent != null && intent.hasExtra(BUDDY_KEY_EXTRA)) {
            if(intent.hasExtra(POST_KEY_EXTRA)) {
                postKey = intent.getStringExtra(POST_KEY_EXTRA)
                postInfo = Post(intent.getSerializableExtra(POST_INFO_EXTRA) as PostInfo)
            }
            buddyKey = intent.getStringExtra(BUDDY_KEY_EXTRA)
            buddyInfo = intent.getSerializableExtra(BUDDY_INFO_EXTRA) as BuddyInfo
        }
    }

    private fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
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

        post_add_image.visibility = View.VISIBLE
        post_add_image.setOnClickListener { pickImage() }

        post_add_location.setOnClickListener { pickPlace() }
        post_add_mark.setOnClickListener {  }

        post_post_button.setOnClickListener {
            if(postKey != null) {
                updatePost(postKey)
            } else {
                createNewPost()
            }
        }

        val postToEdit = postInfo
        if(postToEdit != null) {
            post_message.setText(postToEdit.message)

            val postLocation = postToEdit.location
            if(postLocation.isNotEmpty()) {
                mapsPlace = postLocation
                post_location.text = postLocation
                post_location.visibility = View.VISIBLE
            }

            if(postToEdit.photo.isNotEmpty()) {
                Log.d(TAG, "Photo " + photoUrl)
                Picasso.with(this)
                        .load(postToEdit.photo)
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .fit()
                        .centerCrop()
                        .into(post_photo)
            }
        }
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

    fun createNewPost() {
        val newPost = getPost()
        if(newPost != null) {
            firebaseService.addPost(newPost)
            Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Post error!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    fun updatePost(postKey: String?) {
        val updatePost = getPost()
        val postId = postKey

        if(updatePost != null && postId != null) {
            firebaseService.updatePost(updatePost, postId)
            Toast.makeText(this, "Post updated!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Post error!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    fun getPost(): Post? {
        val id = buddyKey

        if (id != null) {
            return Post(
                    id,
                    post_message.text.toString(),
                    photoUrl,
                    mapsPlace)
        }

        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Activity request code " + requestCode)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                RC_PHOTO_PICKER -> {
                    photoUrl = data?.data.toString()
                    post_photo.setImageBitmap(MediaStore.Images.Media.getBitmap(contentResolver, data?.data))
                }
                PLACE_PICKER_REQUEST -> {
                    mapsPlace = PlacePicker.getPlace(this, data)?.name.toString()
                    post_location.text = mapsPlace
                    post_location.visibility = View.VISIBLE
                }
            }
        }
    }

}