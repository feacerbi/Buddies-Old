package br.com.felipeacerbi.buddies.adapters

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
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
import org.parceler.Parcels

class PostsTabAdapter(val listener: IListClickListener, val userPostsReference: Query, val postsReference: DatabaseReference, val progressBar: ProgressBar) :
        FirebaseIndexRecyclerAdapter<Post, PostsTabAdapter.PostViewHolder>
        (
                Post::class.java,
                R.layout.post_list_item,
                PostViewHolder::class.java,
                userPostsReference,
                postsReference
        ) {

    companion object {
        val TAG = "PostsTabAdapter"
        val HEADER_FOOTER_VIEW_TYPE = 1
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: PostViewHolder, post: Post, position: Int) {
        if(post.petId.isNotEmpty()) {

            firebaseService.getPetReference(post.petId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Log.d(TAG, "Fail to retrieve pet")
                }

                override fun onDataChange(buddySnapshot: DataSnapshot?) {
                    if (buddySnapshot?.value != null) {
                        val buddy = Buddy(buddySnapshot)

                        val editable = buddy.owners.keys.contains(firebaseService.getCurrentUserUID())

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

                            if (editable) {
                                post_more.visibility = View.VISIBLE
                                post_more.setOnClickListener {
                                    val popup = PopupMenu(listener.getContext(), post_more)
                                    popup.setOnMenuItemClickListener { menuItem ->
                                        onActionsMenuClicked(menuItem, position, buddySnapshot.key, buddy)
                                    }
                                    popup.inflate(R.menu.post_actions)
                                    popup.show()
                                }
                            } else {
                                post_more.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            })

            with(holder.itemView) {
                if (post.message.isEmpty()) {
                    post_message.visibility = View.GONE
                } else {
                    post_message.visibility = View.VISIBLE
                    post_message.text = post.message
                }

                if (post.photo.isEmpty()) {
                    post_photo.visibility = View.GONE
                    post_message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
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

                if (post.location.isEmpty() || post.location == "null") {
                    post_location.visibility = View.GONE
                    post_separator.visibility = View.GONE
                } else {
                    post_location.visibility = View.VISIBLE
                    post_separator.visibility = View.VISIBLE
                    post_location.text = post.location
                    post_location.setOnClickListener {
                        openMapAddress(post.location)
                    }
                }

                post_comment_button.setOnClickListener {
                    openCommentsActivity(position)
                }

                if (post.likes.isEmpty()) {
                    post_likes_number.visibility = View.GONE
                } else {
                    post_likes_number.visibility = View.VISIBLE
                    post_likes_number.text = post.likes.size.toString()
                }

                val hasComments = post.comments.isNotEmpty()

                post_new_comment.visibility = if (hasComments) View.INVISIBLE else View.VISIBLE
                post_new_comment_button.visibility = if (hasComments) View.INVISIBLE else View.VISIBLE
                post_comments_number.visibility = if (hasComments) View.VISIBLE else View.GONE
                last_comment_poster_name.visibility = if (hasComments) View.VISIBLE else View.INVISIBLE
                last_comment_poster_comment.visibility = if (hasComments) View.VISIBLE else View.INVISIBLE

                if (post.comments.isEmpty()) {

                    post_new_comment.addTextChangedListener(object : TextWatcher {
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
                        if (newComment != null) firebaseService.addComment(newComment)
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

                if (post.likes.contains(firebaseService.getCurrentUserUID())) {
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

                if (post.withs.isEmpty()) {
                    post_with.visibility = View.GONE
                    post_with_names.visibility = View.GONE
                } else {
                    post_with.visibility = View.VISIBLE
                    post_with_names.visibility = View.VISIBLE

                    if (post.withs.size == 1) {
                        displayWithName(post.withs.keys.elementAt(0), post_with_names)
                    } else {
                        val withText = post.withs.size.toString() + " others"
                        post_with_names.text = withText
                    }
                }
            }
        }
    }

    fun displayWithName(key: String, nameField: TextView) {
        firebaseService.getPetReference(key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.e(TAG, "Could not find buddy")
            }

            override fun onDataChange(userSnapshot: DataSnapshot?) {
                if(userSnapshot != null && userSnapshot.hasChildren()) {
                    val buddy = Buddy(userSnapshot)

                    nameField.text = buddy.name
                }
            }
        })
    }

    fun onActionsMenuClicked(item: MenuItem, position: Int, buddyKey: String, buddy: Buddy): Boolean {
        when(item.itemId) {
            R.id.menu_post_edit -> {
                openEditPost(getRef(position).key, getItem(position), buddyKey, buddy)
                return true
            }
            R.id.menu_post_remove -> {
                firebaseService.removePetPost(getItem(position).petId, getRef(position).key)
                return true
            }
            else -> return false
        }
    }

    private fun openEditPost(postKey: String, post: Post, buddyKey: String, buddy: Buddy) {
        listener.onListClick<NewPostActivity>(
                NewPostActivity::class,
                arrayOf(NewPostActivity.POST_KEY_EXTRA, NewPostActivity.POST_INFO_EXTRA, NewPostActivity.BUDDY_KEY_EXTRA, NewPostActivity.BUDDY_INFO_EXTRA),
                arrayOf(postKey, Parcels.wrap(post), buddyKey, Parcels.wrap(buddy)))
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
                arrayOf(BuddyProfileActivity.EXTRA_PETID),
                arrayOf(post.petId))
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

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PostViewHolder {
        if(viewType == HEADER_FOOTER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.header_footer, parent, false)
            view.setBackgroundColor(listener.getContext().resources.getColor(R.color.profile_picture_background))
            return HeaderFooterViewHolder(view)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0) return HEADER_FOOTER_VIEW_TYPE
        return super.getItemViewType(position)
    }

    override fun getItem(position: Int): Post {
        if(itemCount > 1 && position > 0) {
            return super.getItem(position - 1)
        } else {
            return Post()
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getRef(position: Int): DatabaseReference {
        if(itemCount > 1 && position > 0) {
            return super.getRef(position - 1)
        }
        return firebaseService.getDatabaseReference("")
    }

    open class PostViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class HeaderFooterViewHolder(view: View) : PostViewHolder(view)
}