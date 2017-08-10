package br.com.felipeacerbi.buddies.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.PlaceActivity
import br.com.felipeacerbi.buddies.activities.SuggestPlaceActivity
import br.com.felipeacerbi.buddies.adapters.PlacesAdapter
import br.com.felipeacerbi.buddies.utils.launchActivityForResult
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.setUp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.places_list.view.*


/**
 * A fragment representing a list of Items.
 *
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class PlacesListFragment : PetsListFragment() {

    companion object {
        val SUGGEST_PLACE = 300
    }

    val placesFab by lazy {
        activity.fab
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.places_list, container, false)

        // Set the adapter
        if(view is RelativeLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager (context)
                list.adapter = PlacesAdapter(this@PlacesListFragment, ref, progress)
                list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            placesFab?.hide()
                            placesFab?.isClickable = false
                        } else {
                            placesFab?.show()
                            placesFab?.isClickable = true
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
        placesFab?.setUp(activity, show, R.drawable.ic_add_location_white_24dp) {
            activity.launchActivityForResult(SuggestPlaceActivity::class, SUGGEST_PLACE)
        }
    }

    override fun onListClick(identifiers: Array<Any>?) {
        activity.launchActivityWithExtras(
                PlaceActivity::class,
                arrayOf(PlaceActivity.EXTRA_PLACEID),
                identifiers)
    }
}
