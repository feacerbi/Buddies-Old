package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.BuddiesMultiListAdapter
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.Post
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.showConfirmAdapterDialog
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_new_post.*
import org.parceler.Parcels

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
    var postIntent: Post? = null

    var buddyKey: String? = null
    var buddyIntent: Buddy? = null

    var photoUrl: String = ""
    var mapsPlace: String = ""
    var withBuddies: Map<String, Boolean> = HashMap()

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
                postIntent = Parcels.unwrap(intent.extras.get(POST_INFO_EXTRA) as Parcelable)
            }
            buddyKey = intent.getStringExtra(BUDDY_KEY_EXTRA)
            buddyIntent = Parcels.unwrap(intent.extras.get(BUDDY_INFO_EXTRA) as Parcelable)
        }
    }

    private fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        poster_name.text = buddyIntent?.name
        post_post_button.isEnabled = false
        post_location.visibility = View.GONE

        val info = buddyIntent
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
        post_add_mark.setOnClickListener { pickWiths() }

        post_post_button.setOnClickListener {
            if(postKey != null) {
                updatePost(postKey)
            } else {
                createNewPost()
            }
        }

        val postToEdit = postIntent
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

            withBuddies = postToEdit.withs
            setWiths()
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

    private fun pickWiths() {
        showPetsFollowingDialog()
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
                    mapsPlace,
                    withs = withBuddies)
        }

        return null
    }

    fun showPetsFollowingDialog() {
        firebaseService.queryFollow().addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to get follows")
            }

            override fun onDataChange(followsSnapshot: DataSnapshot?) {
                if (followsSnapshot != null && followsSnapshot.hasChildren()) {

                    val adapter = BuddiesMultiListAdapter(
                            this@NewPostActivity,
                            followsSnapshot.children.map { it.key }.toTypedArray(),
                            withBuddies.keys.toTypedArray())

                    followsSnapshot.children.forEach {
                        addBuddyToList(it.key, followsSnapshot, adapter)
                    }
                }
            }
        })
    }

    fun addBuddyToList(key: String, followsSnapshot: DataSnapshot, adapter: BuddiesMultiListAdapter) {
        firebaseService.getPetReference(key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to get buddy")
            }

            override fun onDataChange(buddySnapshot: DataSnapshot?) {
                if (buddySnapshot != null && buddySnapshot.hasChildren()) {
                    val newBuddy = Buddy(buddySnapshot)
                    adapter.add(newBuddy)

                    checkAndOpenBuddiesDialog(adapter, followsSnapshot)
                }
            }
        })
    }

    private fun checkAndOpenBuddiesDialog(adapter: BuddiesMultiListAdapter, followsSnapshot: DataSnapshot) {
        val total = followsSnapshot.childrenCount
        if(adapter.count.toLong() == total) {
            if(total == 1L) {
                val postBuddyId = followsSnapshot.children.elementAt(0).key
                withBuddies = mapOf(Pair(postBuddyId, true))
            } else {
                AlertDialog.Builder(this).showConfirmAdapterDialog(
                        "Choose a Buddy to post",
                        adapter,
                        "Add",
                        { dialog, position ->
                            adapter.check(followsSnapshot.children.elementAt(position).key)
                        },
                        {
                            dialog, position ->
                            withBuddies = adapter.getSelectedKeys()
                            setWiths()
                        })
            }
        }
    }

    fun setWiths() {
        if(withBuddies.isEmpty()) {
            post_with.visibility = View.GONE
            post_with_names.visibility = View.GONE
        } else {
            post_with.visibility = View.VISIBLE
            post_with_names.visibility = View.VISIBLE

            var withText = " "

            if (withBuddies.size == 1) {
                displayWithName(withBuddies.keys.elementAt(0), post_with_names)
            } else {
                withText += withBuddies.size.toString() + " others"
                post_with_names.text = withText
            }
        }
    }

    fun displayWithName(key: String, nameField: TextView) {
        firebaseService.getUserReference(key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e(TAG, "Could not find user")
            }

            override fun onDataChange(userSnapshot: DataSnapshot?) {
                if(userSnapshot != null && userSnapshot.hasChildren()) {
                    val user = User(userSnapshot)

                    val nameText = " " + user.name
                    nameField.text = nameText
                }
            }
        })
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