package br.com.felipeacerbi.buddies.adapters

import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.BuddyProfileActivity
import br.com.felipeacerbi.buddies.activities.CommentsActivity
import br.com.felipeacerbi.buddies.activities.FullscreenPhotoActivity
import br.com.felipeacerbi.buddies.activities.NewPostActivity
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
                        .placeholder(R.drawable.no_phototn)
                        .error(R.drawable.no_phototn)
                        .fit()
                        .centerInside()
                        .into(post_photo)
            }

            post_timestamp.text = post.created.toFormatedDate()

            if(post.location.isEmpty()) {
                post_separator.visibility = View.GONE
            } else {
                post_separator.visibility = View.VISIBLE
                post_location.text = post.location
            }

            post_header.setOnClickListener {
                listener.onListClick<BuddyProfileActivity>(
                        BuddyProfileActivity::class,
                        arrayOf(BuddyProfileActivity.EXTRA_PETID, BuddyProfileActivity.EXTRA_EDITABLE) ,
                        arrayOf(post.petId, false))
            }

            post_comment_button.setOnClickListener {
                openCommentsActivity(position)
            }

            post_share_button.setOnClickListener {
                openNewPostActivity(position)
            }

            if(post.likes.size == 0) {
                post_likes_number.visibility = View.GONE
            } else {
                post_likes_number.visibility = View.VISIBLE
                post_likes_number.text = post.likes.size.toString()
            }

            if(post.comments.size == 0) {
                post_comments_number.visibility = View.GONE
                last_comment_view.visibility = View.GONE
            } else {
                post_comments_number.visibility = View.VISIBLE
                post_comments_number.text = post.comments.size.toString()

                last_comment_view.visibility = View.VISIBLE
                last_comment_view.setOnClickListener {
                    openCommentsActivity(position)
                }
                setUpLastComment(post.comments.keys.elementAt(0), holder)
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

    fun setUpLastComment(postKey: String, holder: PostViewHolder) {
        firebaseService.getCommentReference(postKey).addListenerForSingleValueEvent(object: ValueEventListener {
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

    fun openCommentsActivity(position: Int) {
        listener.onListClick<CommentsActivity>(
                CommentsActivity::class,
                arrayOf(CommentsActivity.POST_ID_EXTRA),
                arrayOf(getRef(position).key))
    }

    fun openNewPostActivity(position: Int) {
        listener.onListClick<NewPostActivity>(
                NewPostActivity::class,
                arrayOf(NewPostActivity.SHARE_POST_ID),
                arrayOf(getRef(position).key))
    }

    override fun onDataChanged() {
        super.onDataChanged()
        hideProgressBar()
    }

    override fun onChildChanged(type: ChangeEventListener.EventType?, snapshot: DataSnapshot?, index: Int, oldIndex: Int) {
        super.onChildChanged(type, snapshot, index, oldIndex)
        hideProgressBar()
        listener.selectListItem(itemCount - 1)
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view)
}