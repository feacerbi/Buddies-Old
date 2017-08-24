package br.com.felipeacerbi.buddies.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Comment
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.toFormatedDate
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_list_item.view.*

class CommentsAdapter(val context: Context, val postCommentsReference: DatabaseReference, val commentsReference: DatabaseReference, val progressBar: ProgressBar) :
        FirebaseIndexRecyclerAdapter<Comment, CommentsAdapter.CommentViewHolder>
        (
                Comment::class.java,
                R.layout.comment_list_item,
                CommentViewHolder::class.java,
                postCommentsReference,
                commentsReference

        ) {

    companion object {
        val TAG = "CommentsAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: CommentViewHolder, comment: Comment, position: Int) {

        firebaseService.getUserReference(comment.posterId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve user")
            }

            override fun onDataChange(userSnapshot: DataSnapshot?) {
                if(userSnapshot?.value != null) {
                    val user = User(userSnapshot)

                    with(holder.itemView) {
                        poster_name.text = user.name

                        Picasso.with(context)
                                .load(user.photo)
                                .placeholder(R.drawable.no_phototn)
                                .error(R.drawable.no_phototn)
                                .fit()
                                .centerCrop()
                                .into(poster_profile_photo)
                    }
                }
            }
        })

        val reqTime = comment.timestamp.toFormatedDate()

        with(holder.itemView) {
            comment_timestamp.text = reqTime
            comment_message.text = comment.message
            setCommentLikesText(comment, this)

            comment_like_button.setOnClickListener {
                firebaseService.addCommentLike(getRef(position).key)
            }

            if(comment.likes.contains(firebaseService.getCurrentUserUID())) {
                comment_like_button.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))

                comment_like_button.setOnClickListener {
                    firebaseService.removeCommentLike(getRef(position).key)
                }
            } else {
                comment_like_button.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))

                comment_like_button.setOnClickListener {
                    firebaseService.addCommentLike(getRef(position).key)
                }
            }
        }
    }

    fun setCommentLikesText(comment: Comment, view: View) {
        with(view) {
            val likesCount = comment.likes.size
            if (likesCount == 0) {
                comment_likes.visibility = View.GONE
                comment_separator.visibility = View.GONE
            } else {
                comment_likes.visibility = View.VISIBLE
                comment_separator.visibility = View.VISIBLE

                var likesText = comment.likes.size.toString()

                if (likesCount == 1) {
                    likesText += " like"
                } else {
                    likesText += " likes"
                }

                comment_likes.text = likesText
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

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view)
}