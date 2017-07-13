package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.R
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.buddy_list_item.view.*

/**
 * Created by felipe.acerbi on 04/07/2017.
 */

class BuddiesAdapter(val petsReference: DatabaseReference) :
        FirebaseRecyclerAdapter<Boolean, BuddiesAdapter.BuddyViewHolder>
        (
                Boolean::class.java,
                R.layout.buddy_list_item,
                BuddyViewHolder::class.java,
                petsReference
        ) {

    companion object {
        val TAG = "BuddiesAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: BuddyViewHolder?, item: Boolean?, position: Int) {
        val petId = getRef(position).key

        firebaseService.getPetReference(petId).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve pet")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.d(TAG, "New data")
                if(dataSnapshot != null && holder != null) {
                    val buddy = Buddy(dataSnapshot)
                    Log.d(TAG, "Load buddy " + buddy.name)

                    with(holder.itemView) {
                        name.text = buddy.name
                        breed.text = buddy.breed
                        remove_button.setOnClickListener { firebaseService.removePet(petsReference.key, petId) }
                    }
                }
            }
        })
    }

    class BuddyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}