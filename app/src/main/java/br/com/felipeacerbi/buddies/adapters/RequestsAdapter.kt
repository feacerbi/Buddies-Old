package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.models.Request
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.request_list_item.view.*

/**
 * Created by felipe.acerbi on 04/07/2017.
 */

class RequestsAdapter(val petsReference: DatabaseReference) :
        FirebaseRecyclerAdapter<Boolean, RequestsAdapter.RequestViewHolder>
        (
                Boolean::class.java,
                R.layout.request_list_item,
                RequestViewHolder::class.java,
                petsReference
        ) {

    companion object {
        val TAG = "RequestsAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: RequestViewHolder?, item: Boolean?, position: Int) {
        val requestId = getRef(position).key

        firebaseService.getRequestReference(requestId).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve request")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(dataSnapshot != null && holder != null) {
                    val request = Request(dataSnapshot)

                    with(holder.itemView) {
                        requester_username.text = request.petId
                        requested_pet_id.text = request.username

                        if(request.status == Request.STATUS_OPEN) {
                            status_text.visibility = android.view.View.GONE

                            buttons.visibility = android.view.View.VISIBLE
                            allow_button.setOnClickListener { firebaseService.allowPetOwner(request, getRef(position).key, true) }
                            not_allow_button.setOnClickListener { firebaseService.allowPetOwner(request, getRef(position).key, false) }
                        } else {
                            buttons.visibility = android.view.View.GONE

                            status_text.visibility = android.view.View.VISIBLE
                            status_text.text = request.status
                        }

                    }
                }
            }
        })
    }

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view)
}