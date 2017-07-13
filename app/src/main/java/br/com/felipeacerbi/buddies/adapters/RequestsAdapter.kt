package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.models.Request
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.buddy_list_item.view.*
import kotlinx.android.synthetic.main.request_list_item.view.*

/**
 * Created by felipe.acerbi on 04/07/2017.
 */

class RequestsAdapter(val petsReference: DatabaseReference) :
        FirebaseRecyclerAdapter<Request, RequestsAdapter.RequestViewHolder>
        (
                Request::class.java,
                R.layout.buddy_list_item,
                RequestViewHolder::class.java,
                petsReference
        ) {

    companion object {
        val TAG = "RequestsAdapter"
    }

    val firebaseService = FirebaseService()
    val retrieveUserPetsListener = UserPetsListener()

    init {
        firebaseService.getUserPetsReference(firebaseService.getCurrentUsername()).addValueEventListener(retrieveUserPetsListener)
    }

    override fun populateViewHolder(holder: RequestViewHolder?, request: Request?, position: Int) {
        if(holder != null && request != null) {
            with(holder.itemView) {
                requester_username.text = request.petId
                requested_pet_id.text = request.username
                allow_button.setOnClickListener { firebaseService.allowPetOwner(request, getRef(position).key, true) }
                not_allow_button.setOnClickListener { firebaseService.allowPetOwner(request, getRef(position).key, false) }
            }
        }
    }

    override fun cleanup() {
        super.cleanup()
        firebaseService.getUserPetsReference(firebaseService.getCurrentUsername()).removeEventListener(retrieveUserPetsListener)
    }

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class UserPetsListener: ValueEventListener {
        override fun onCancelled(error: DatabaseError?) {
            Log.d(TAG, "Retrieving owner pets cancelled " + error?.message)
        }

        override fun onDataChange(dataSnapshot: DataSnapshot?) {
            if(dataSnapshot != null && dataSnapshot.childrenCount > 0) {
                mSnapshots.removeAll(mSnapshots.filter { it.child("petId").key !in dataSnapshot.children.map { it.key } })
            }
        }
    }
}