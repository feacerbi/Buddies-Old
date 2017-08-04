package br.com.felipeacerbi.buddies.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.PlaceActivity
import br.com.felipeacerbi.buddies.adapters.PlacesAdapter
import br.com.felipeacerbi.buddies.models.Place
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        setUpFab(true)
    }

    override fun onResume() {
        super.onResume()
        setUpFab(true)
    }

    override fun setUpFab(show: Boolean) {
        placesFab?.setUp(activity, show, R.drawable.ic_add_location_white_24dp) {
            // Test place
            val place = Place(
                    "Villa Grano",
                    "Rua Wisard, 342",
                    "",
                    "Nice bakery.",
                    "Bakery"
            )
            firebaseService.addPlace(place)
        }
    }

    override fun onListClick(identifiers: Array<Any>?) {
        activity.launchActivityWithExtras(
                PlaceActivity::class,
                arrayOf(PlaceActivity.EXTRA_PLACEID),
                identifiers)
    }
}
