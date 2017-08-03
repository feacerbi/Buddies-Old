package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Place
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.place_list_item.view.*

class PlacesAdapter(val listener: IListClickListener, val placesReference: DatabaseReference, val progressBar: ProgressBar) :
        FirebaseRecyclerAdapter<Place, PlacesAdapter.PlaceViewHolder>
        (
                Place::class.java,
                R.layout.place_list_item,
                PlaceViewHolder::class.java,
                placesReference
        ) {

    companion object {
        val TAG = "BuddiesAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: PlaceViewHolder?, place: Place?, position: Int) {
        if(holder != null && place != null) {
            with(holder.itemView) {
                place_name.text = place.name
                place_category.text = place.category
                place_meter.progress = place.rating
//                place_distance.text = calculateDistance()

                if(place.photo.isNotEmpty()) {
                    Picasso.with(listener.getContext())
                            .load(place.photo)
                            .placeholder(R.drawable.no_phototn)
                            .error(R.drawable.no_phototn)
                            .fit()
                            .centerCrop()
                            .into(place_photo)
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

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view)
}