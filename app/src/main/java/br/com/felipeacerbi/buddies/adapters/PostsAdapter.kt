package br.com.felipeacerbi.buddies.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.BuddyProfileActivity
import br.com.felipeacerbi.buddies.activities.CommentsActivity
import br.com.felipeacerbi.buddies.activities.FullscreenPhotoActivity
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.Comment
import br.com.felipeacerbi.buddies.models.Post
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.toFormatedDate
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_list_item.view.*

class PostsAdapter(val listener: IListClickListener, val userPostsReference: Query, val postsReference: DatabaseReference, val progressBar: ProgressBar) :
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
                        poster_name.setOnClickListener {
                            openBuddyDetails(post)
                        }

                        post_profile_photo.setOnClickListener {
                            openBuddyDetails(post)
                        }

                        Picasso.with(context)
                                .load(buddy.photo)
                                .placeholder(R.drawable.no_phototn)
                                .error(R.drawable.no_phototn)
                                .fit()
                                .centerCrop()
                                .into(post_profile_photo)

                        post_photo.setOnClickListener {
                            listener.onListClick<FullscreenPhotoActivity>(
                                    FullscreenPhotoActivity::class,
                                    arrayOf(FullscreenPhotoActivity.PHOTO_PATH, FullscreenPhotoActivity.PHOTO_MESSAGE, FullscreenPhotoActivity.TOOLBAR_TITLE),
                                    arrayOf(post.photo, post.message, buddy.name))
                        }
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
                post_message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22F)
            } else {
                post_photo.visibility = View.VISIBLE
                post_message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
                Picasso.with(context)
                        .load(post.photo)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .fit()
                        .centerInside()
                        .into(post_photo)
            }

            post_timestamp.text = post.created.toFormatedDate()

            if(post.location.isEmpty() || post.location == "null") {
                post_separator.visibility = View.GONE
            } else {
                post_separator.visibility = View.VISIBLE
                post_location.text = post.location
                post_location.setOnClickListener {
                    openMapAddress(post.location)
                }
            }

            post_comment_button.setOnClickListener {
                openCommentsActivity(position)
            }

            if(post.likes.isEmpty()) {
                post_likes_number.visibility = View.GONE
            } else {
                post_likes_number.visibility = View.VISIBLE
                post_likes_number.text = post.likes.size.toString()
            }

            val hasComments = post.comments.isNotEmpty()

            post_new_comment.visibility = if(hasComments) View.INVISIBLE else View.VISIBLE
            post_new_comment_button.visibility = if(hasComments) View.INVISIBLE else View.VISIBLE
            post_comments_number.visibility = if(hasComments) View.VISIBLE else View.GONE
            last_comment_poster_name.visibility = if(hasComments) View.VISIBLE else View.INVISIBLE
            last_comment_poster_comment.visibility = if(hasComments) View.VISIBLE else View.INVISIBLE

            if(post.comments.isEmpty()) {

                post_new_comment.addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {
                        // No need
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        // No need
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, size: Int) {
                        post_new_comment_button.isEnabled = size != 0
                    }
                })

                post_new_comment_button.setOnClickListener {
                    val newComment = createComment(position, post_new_comment)
                    if(newComment != null) firebaseService.addComment(newComment)
                    post_new_comment.setText("")
                }

                last_comment_poster_comment.setOnClickListener(null)
                setUpNewComment(holder)
            } else {
                post_comments_number.text = post.comments.size.toString()

                last_comment_view.setOnClickListener {
                    openCommentsActivity(position)
                }

                val commentKeys = post.comments.keys
                setUpLastComment(commentKeys.elementAt(commentKeys.size - 1), holder)
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

    private fun setUpNewComment(holder: PostViewHolder) {
        firebaseService.getUserReference(firebaseService.getCurrentUserUID()).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e(TAG, "Could not find user")
            }

            override fun onDataChange(userSnapshot: DataSnapshot?) {
                if(userSnapshot != null && userSnapshot.hasChildren()) {
                    val poster = User(userSnapshot)

                    with(holder.itemView) {
                        Picasso.with(listener.getContext())
                                .load(poster.photo)
                                .placeholder(R.drawable.no_phototn)
                                .error(R.drawable.no_phototn)
                                .fit()
                                .centerCrop()
                                .into(last_comment_profile_photo)
                    }
                }
            }
        })
    }

    fun setUpLastComment(commentKey: String, holder: PostViewHolder) {
        firebaseService.getCommentReference(commentKey).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e(TAG, "Could not find comment")
            }

            override fun onDataChange(commentSnapshot: DataSnapshot?) {
                if(commentSnapshot != null && commentSnapshot.hasChildren()) {
                    val lastComment = Comment(commentSnapshot)

                    with(holder.itemView) {
                        val comment = " " + lastComment.message
                        last_comment_poster_comment.text = comment
                        firebaseService.getUserReference(lastComment.posterId).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {
                                Log.e(TAG, "Could not find user")
                            }

                            override fun onDataChange(userSnapshot: DataSnapshot?) {
                                if(userSnapshot != null && userSnapshot.hasChildren()) {
                                    val poster = User(userSnapshot)
                                    last_comment_poster_name.text = poster.name

                                    Picasso.with(listener.getContext())
                                            .load(poster.photo)
                                            .placeholder(R.drawable.no_phototn)
                                            .error(R.drawable.no_phototn)
                                            .fit()
                                            .centerCrop()
                                            .into(last_comment_profile_photo)
                                }
                            }
                        })
                    }

                }
            }
        })
    }

    fun createComment(position: Int, view: EditText): Comment? {
        val id = getRef(position).key

        if(id != null) {
            return Comment(postId = id, message = view.text.toString())
        }

        return null
    }

    fun openBuddyDetails(post: Post) {
        listener.onListClick<BuddyProfileActivity>(
                BuddyProfileActivity::class,
                arrayOf(BuddyProfileActivity.EXTRA_PETID, BuddyProfileActivity.EXTRA_EDITABLE) ,
                arrayOf(post.petId, false))
    }

    fun openCommentsActivity(position: Int) {
        listener.onListClick<CommentsActivity>(
                CommentsActivity::class,
                arrayOf(CommentsActivity.POST_ID_EXTRA),
                arrayOf(getRef(position).key))
    }

    fun openMapAddress(address: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("geo:0,0?q=" + address)
        if (intent.resolveActivity(listener.getContext().packageManager) != null) {
            listener.getContext().startActivity(intent)
        }
    }

    override fun onDataChanged() {
        super.onDataChanged()
        hideProgressBar()
    }

    override fun onChildChanged(type: ChangeEventListener.EventType?, snapshot: DataSnapshot?, index: Int, oldIndex: Int) {
        super.onChildChanged(type, snapshot, index, oldIndex)
        hideProgressBar()
        if(type == ChangeEventListener.EventType.ADDED) {
            listener.selectListItem(itemCount - 1)
        }
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view)
}