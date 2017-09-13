package br.com.felipeacerbi.buddies.fragments

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.NewPostActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesListAdapter
import br.com.felipeacerbi.buddies.adapters.PostsAdapter
import br.com.felipeacerbi.buddies.adapters.PostsTabAdapter
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.setUp
import br.com.felipeacerbi.buddies.utils.showAdapterDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.posts_list.*
import kotlinx.android.synthetic.main.posts_list.view.*
import org.parceler.Parcels
import kotlin.reflect.KClass


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
        val tab = container is ViewPager

        var view = inflater?.inflate(R.layout.posts_list, container, false)
        if(tab) {
            view = inflater?.inflate(R.layout.tab_posts_list, container, false)
        } else {
            parentActivity.setSupportActionBar(view?.toolbar)
        }

        // Set the adapter
        if(view is ConstraintLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager(context)
                (list.layoutManager as LinearLayoutManager).reverseLayout = true
                if(tab) {
                    list.adapter = PostsTabAdapter(this@PostsListFragment, ref, firebaseService.getPostsReference(), progress)
                } else {
                    list.adapter = PostsAdapter(this@PostsListFragment, ref, firebaseService.getPostsReference(), progress)
                }
                list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            postsFab?.hide()
                            postsFab?.isClickable = false
//                            parentActivity.supportActionBar?.hide()
                        } else {
                            postsFab?.show()
                            postsFab?.isClickable = true
//                            parentActivity.supportActionBar?.show()
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

            override fun onDataChange(ownedsSnapshot: DataSnapshot?) {
                if (ownedsSnapshot != null && ownedsSnapshot.hasChildren()) {

                    val adapter = BuddiesListAdapter(activity)

                    ownedsSnapshot.children.forEach {
                        addBuddyToList(it.key, ownedsSnapshot, adapter)
                    }
                }
            }
        })
    }

    fun addBuddyToList(key: String, ownedsSnapshot: DataSnapshot, adapter: BuddiesListAdapter) {
        firebaseService.getPetReference(key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to get buddy")
            }

            override fun onDataChange(buddySnapshot: DataSnapshot?) {
                if (buddySnapshot != null && buddySnapshot.hasChildren()) {
                    val newBuddy = Buddy(buddySnapshot)

                    adapter.add(newBuddy)

                    checkAndOpenBuddiesDialog(adapter, ownedsSnapshot)
                }
            }
        })
    }

    private fun checkAndOpenBuddiesDialog(adapter: BuddiesListAdapter, ownedsSnapshot: DataSnapshot) {
        val total = ownedsSnapshot.childrenCount
        if(adapter.count.toLong() == total) {
            if(total == 1L) {
                val postBuddyId = ownedsSnapshot.children.elementAt(0).key
                openNewPostActivity(postBuddyId, adapter.getItem(0))
            } else {
                AlertDialog.Builder(context).showAdapterDialog(
                        "Choose a Buddy to post",
                        adapter,
                        { dialog, position ->
                            val postBuddyId = ownedsSnapshot.children.elementAt(position).key
                            openNewPostActivity(postBuddyId, adapter.getItem(position))
                            dialog.dismiss()
                        })
            }
        }
    }

    fun openNewPostActivity(key: String, buddy: Buddy) {
        activity.launchActivityWithExtras<NewPostActivity>(
                NewPostActivity::class,
                arrayOf(NewPostActivity.BUDDY_KEY_EXTRA, NewPostActivity.BUDDY_INFO_EXTRA),
                arrayOf(key, Parcels.wrap(buddy)))
    }

    override fun setUpFab(show: Boolean) {
        postsFab?.setUp(activity, show, R.drawable.ic_mode_edit_white_24dp) { showPetsOwnedDialog() }
    }

    override fun <T : Any> onListClick(clazz: KClass<T>, identifiers: Array<String>?, extras: Array<Any>?) {
        activity.launchActivityWithExtras(
                clazz,
                identifiers,
                extras)
    }

    override fun selectListItem(position: Int) {
        list?.scrollToPosition(position)
    }

    override fun getViewInflater() = activity.layoutInflater
}
