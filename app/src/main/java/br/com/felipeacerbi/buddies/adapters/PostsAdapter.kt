package br.com.felipeacerbi.buddies.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.Post
import br.com.felipeacerbi.buddies.utils.toFormatedDate
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_list_item.view.*

class PostsAdapter(val context: Context, val userPostsReference: DatabaseReference, val postsReference: DatabaseReference, val progressBar: ProgressBar) :
        FirebaseIndexRecyclerAdapter<Post, PostsAdapter.PostViewHolder>
        (
                Post::class.java,
                R.layout.post_list_item,
                PostViewHolder::class.java,
                userPostsReference,
                postsReference
        ) {

    companion object {
        val TAG = "RequestsAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: PostViewHolder, post: Post, position: Int) {
        firebaseService.getPetReference(post.petId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve pet")
            }

            override fun onDataChange(buddySnapshot: DataSnapshot?) {
                if(buddySnapshot?.value != null) {
                    val buddy = Buddy(buddySnapshot)

                    with(holder.itemView) {
                        poster_name.text = buddy.name

                        Picasso.with(context)
                                .load(buddy.photo)
                                .placeholder(R.drawable.no_phototn)
                                .error(R.drawable.no_phototn)
                                .fit()
                                .centerCrop()
                                .into(post_profile_photo)
                    }
                }
            }
        })

        with(holder.itemView) {
            if(post.message.isEmpty()) {
                post_message.visibility = View.GONE
            } else {
                post_message.visibility = View.VISIBLE
                post_message.text = post.message
            }

            if(post.photo.isEmpty()) {
                post_photo.visibility = View.GONE
            } else {
                post_photo.visibility = View.VISIBLE
                Picasso.with(context)
                        .load(post.photo)
                        .placeholder(R.drawable.no_phototn)
                        .error(R.drawable.no_phototn)
                        .fit()
                        .centerCrop()
                        .into(post_photo)
            }

            post_timestamp.text = post.created.toFormatedDate()

            if(post.location.isEmpty()) {
                post_separator.visibility = View.GONE
            } else {
                post_separator.visibility = View.VISIBLE
                post_location.text = post.location
            }

            if(post.likes.contains(firebaseService.getCurrentUserUID())) {
                post_like_button.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))

                post_like_button.setOnClickListener {
                    firebaseService.removePostLike(getRef(position).key)
                }
            } else {
                post_like_button.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))

                post_like_button.setOnClickListener {
                    firebaseService.addPostLike(getRef(position).key)
                }
            }
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        hideProgressBar()
    }

    override fun onChildChanged(type: ChangeEventListener.EventType?, snapshot: DataSnapshot?, index: Int, oldIndex: Int) {
        super.onChildChanged(type, snapshot, index, oldIndex)
        hideProgressBar()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view)
}