package br.com.felipeacerbi.buddies.fragments

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.FullscreenPhotoActivity
import br.com.felipeacerbi.buddies.activities.NewPostActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesListAdapter
import br.com.felipeacerbi.buddies.adapters.PostsAdapter
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.setUp
import br.com.felipeacerbi.buddies.utils.showAdapterDialog
import br.com.felipeacerbi.buddies.utils.showCustomDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.buddies_list.view.*


/**
 * A fragment representing a list of Items.
 *
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class PostsListFragment : PetsListFragment() {

    companion object {
        val TAG = "PostsListFragment"
    }

    val postsFab by lazy {
        activity.fab
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.posts_list, container, false)

        // Set the adapter
        if(view is ConstraintLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager(context)
                list.adapter = PostsAdapter(this@PostsListFragment, ref, firebaseService.getPostsReference(), progress)
                list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            postsFab?.hide()
                            postsFab?.isClickable = false
                        } else {
                            postsFab?.show()
                            postsFab?.isClickable = true
                        }
                    }
                })
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setUpFab(true)
    }

    fun showPetsOwnedDialog() {
        firebaseService.queryBuddies().addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to get buddies")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null && dataSnapshot.hasChildren()) {

                    val adapter = BuddiesListAdapter(activity)

                    dataSnapshot.children.forEach {
                        addBuddyToList(it.key, adapter)
                    }

                    AlertDialog.Builder(context).showAdapterDialog(
                            "Choose a Buddy to post",
                            adapter,
                            { dialog, position ->
                                val postBuddyId = dataSnapshot.children.elementAt(position).key
                                openNewPostActivity(postBuddyId, adapter.getItem(position))
                                dialog.dismiss()
                            })
                }
            }
        })
    }

    fun addBuddyToList(key: String, adapter: BuddiesListAdapter) {
        firebaseService.getPetReference(key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to get buddy")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null && dataSnapshot.hasChildren()) {
                    val newBuddy = Buddy(dataSnapshot)
                    adapter.add(newBuddy)
                }
            }
        })
    }

    fun openNewPostActivity(key: String, buddy: Buddy) {
        activity.launchActivityWithExtras<NewPostActivity>(
                NewPostActivity::class,
                arrayOf(NewPostActivity.BUDDY_KEY_EXTRA, NewPostActivity.BUDDY_INFO_EXTRA),
                arrayOf(key, buddy.toBuddyInfo()))
    }

    override fun setUpFab(show: Boolean) {
        postsFab?.setUp(activity, show, R.drawable.plus_sign) { showPetsOwnedDialog() }
    }

    override fun onListClick(identifiers: Array<Any>?) {
        activity.launchActivityWithExtras<FullscreenPhotoActivity>(
                FullscreenPhotoActivity::class,
                arrayOf(FullscreenPhotoActivity.PHOTO_PATH,
                        FullscreenPhotoActivity.PHOTO_MESSAGE,
                        FullscreenPhotoActivity.TOOLBAR_TITLE),
                identifiers)
    }

    override fun getViewInflater() = activity.layoutInflater
}
