package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.PlaceActivity
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.FriendlyItem
import br.com.felipeacerbi.buddies.models.Place
import br.com.felipeacerbi.buddies.utils.toDistanceUnits
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
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
        val HEADER_FOOTER_VIEW_TYPE = 1
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: PlaceViewHolder, item: Long, position: Int) {
        if(item != -1L) {
            val placeKey = getRef(position).key

            firebaseService.getPlaceReference(placeKey).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    Log.d(TAG, "Fail to retrieve place")
                }

                override fun onDataChange(placeSnapshot: DataSnapshot?) {
                    if (placeSnapshot?.value != null && placeSnapshot.hasChildren()) {
                        val place = Place(placeSnapshot)

                        with(holder.itemView) {
                            place_name.text = place.name
                            place_category.text = place.category
                            place_distance.text = item.toDistanceUnits()

                            firebaseService.getPlaceFriendlyItemsReference(getRef(position).key).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError?) {
                                    Log.d(TAG, "Fail to retrieve friendly place")
                                }

                                override fun onDataChange(fitemSnapshot: DataSnapshot?) {
                                    if (fitemSnapshot?.value != null && fitemSnapshot.hasChildren()) {
                                        friendly_items_bottom.removeAllViews()
                                        fitemSnapshot.children.forEach {
                                            friendly_items_bottom.addView(createFriendlyView(it))
                                        }
                                        place_meter.progress = place.calcRating(fitemSnapshot.childrenCount)
                                    }
                                }
                            })

                            if (place.photo.isNotEmpty()) {
                                Picasso.with(listener.getContext())
                                        .load(place.photo)
                                        .placeholder(R.drawable.placeholder)
                                        .error(R.drawable.placeholder)
                                        .fit()
                                        .centerCrop()
                                        .into(place_photo)
                            }

                            setOnClickListener {
                                listener.onListClick<PlaceActivity>(
                                        PlaceActivity::class,
                                        arrayOf(PlaceActivity.EXTRA_PLACEID),
                                        arrayOf(getRef(position).key))
                            }
                        }
                    }
                }
            })
        }
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
                        view.setPaddingRelative(4, 0, 4, 0)
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

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PlaceViewHolder {
        if(viewType == HEADER_FOOTER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.header_footer, parent, false)
            return HeaderFooterViewHolder(view)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0 || position == itemCount - 1) return HEADER_FOOTER_VIEW_TYPE
        return super.getItemViewType(position)
    }

    override fun getItem(position: Int): Long {
        if(itemCount > 2 && position > 0 && position < itemCount - 1) {
            return super.getItem(position - 1)
        } else {
            return -1L
        }
    }

    override fun getRef(position: Int): DatabaseReference {
        if(itemCount > 2 && position > 0 && position < itemCount - 1) {
            return super.getRef(position - 1)
        }
        return firebaseService.getDatabaseReference("")
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 2
    }

    open class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class HeaderFooterViewHolder(view: View) : PlaceViewHolder(view)
}
