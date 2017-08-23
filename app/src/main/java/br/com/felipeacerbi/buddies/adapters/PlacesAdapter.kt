package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.FriendlyItem
import br.com.felipeacerbi.buddies.models.Place
import br.com.felipeacerbi.buddies.utils.toDistanceUnits
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.place_list_item.view.*

class PlacesAdapter(val listener: IListClickListener, val userPlacesReference: Query, val progressBar: ProgressBar) :
        FirebaseRecyclerAdapter<Long, PlacesAdapter.PlaceViewHolder>
        (
                Long::class.java,
                R.layout.place_list_item,
                PlaceViewHolder::class.java,
                userPlacesReference
        ) {

    companion object {
        val TAG = "PlacesAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: PlaceViewHolder, item: Long, position: Int) {
        val placeKey = getRef(position).key

        firebaseService.getPlaceReference(placeKey).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve place")
            }

            override fun onDataChange(placeSnapshot: DataSnapshot?) {
                if(placeSnapshot?.value != null && placeSnapshot.hasChildren()) {
                    val place = Place(placeSnapshot)

                    with(holder.itemView) {
                        place_name.text = place.name
                        place_category.text = place.category
                        place_distance.text = item.toDistanceUnits()

                        firebaseService.getPlaceFriendlyItemsReference(getRef(position).key).addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onCancelled(error: DatabaseError?) {
                                Log.d(TAG, "Fail to retrieve friendly place")
                            }

                            override fun onDataChange(fitemSnapshot: DataSnapshot?) {
                                if(fitemSnapshot?.value != null && fitemSnapshot.hasChildren()) {
                                    friendly_items_bottom.removeAllViews()
                                    fitemSnapshot.children.forEach {
                                        friendly_items_bottom.addView(createFriendlyView(it))
                                    }
                                    place_meter.progress = place.calcRating(fitemSnapshot.childrenCount)
                                }
                            }
                        })

                        if(place.photo.isNotEmpty()) {
                            Picasso.with(listener.getContext())
                                    .load(place.photo)
                                    .placeholder(R.drawable.no_phototn)
                                    .error(R.drawable.no_phototn)
                                    .fit()
                                    .centerCrop()
                                    .into(place_photo)
                        }

                        setOnClickListener { listener.onListClick(arrayOf(getRef(position).key)) }
                    }
                }
            }
        })
    }

    fun createFriendlyView(dataSnapshot: DataSnapshot): View {
        val view = listener.getViewInflater().inflate(R.layout.friendly_item, null, false)

        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.marginStart = 16

        firebaseService.getFriendlyItemReference(dataSnapshot.key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve friendly item")
            }

            override fun onDataChange(itemDataSnapshot: DataSnapshot?) {
                if(itemDataSnapshot?.value != null && itemDataSnapshot.hasChildren()) {
                    val item = FriendlyItem(itemDataSnapshot)
                    with(view as TextView) {
                        text = item.name
                    }
                }
            }
        })

        view.layoutParams = params
        return view
    }

    override fun onDataChanged() {
        super.onDataChanged()
        hideProgressBar()
    }

    override fun onChildChanged(type: ChangeEventListener.EventType?, snapshot: DataSnapshot?, index: Int, oldIndex: Int) {
        super.onChildChanged(type, snapshot, index, oldIndex)
        hideProgressBar()
        listener.selectListItem(0)
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
