package br.com.felipeacerbi.buddies.fragments

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.PostsAdapter
import br.com.felipeacerbi.buddies.models.Post
import br.com.felipeacerbi.buddies.utils.setUp
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
                list.adapter = PostsAdapter(activity, ref, firebaseService.getPostsReference(), progress)
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

    override fun setUpFab(show: Boolean) {
        postsFab?.setUp(activity, show, R.drawable.plus_sign) {
            //permissionsManager.launchWithPermission(Manifest.permission.CAMERA) { activity.launchActivity(QRCodeActivity::class) }
            val new = Post(
                    "-KrYn7zXulpuGqBtlaGN",
                    "Check out these four!",
                    location = "Siberia"
            )
            firebaseService.addPost(new)
        }
    }

    override fun getViewInflater() = activity.layoutInflater
}
