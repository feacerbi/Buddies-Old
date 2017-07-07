package br.com.felipeacerbi.buddies.adapters.delegates

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewTypeDelegateAdapter
import br.com.felipeacerbi.buddies.utils.inflate
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.buddy_list_item.view.*

/**
 * Created by felipe.acerbi on 04/07/2017.
 */

class BuddiesDelegateAdapter(val petsReference: DatabaseReference) : //ViewTypeDelegateAdapter,
        FirebaseRecyclerAdapter<String, BuddiesDelegateAdapter.BuddyViewHolder>
        (
                String::class.java,
                R.layout.buddy_list_item,
                BuddyViewHolder::class.java,
                petsReference
        ) {

    companion object {
        val TAG = "BuddiesDelAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: BuddyViewHolder?, item: String?, position: Int) {
        val petId = getRef(position).key

        firebaseService.getPetReference(petId).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve pet.")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.d(TAG, "New data.")
                if(dataSnapshot != null && holder != null) {
                    val buddy = Buddy(dataSnapshot)
                    Log.d(TAG, "Load buddy " + buddy.name)

                    with(holder.itemView) {
                        name.text = buddy.name
                        breed.text = buddy.breed
                        tagID.text = buddy.tagId
                    }
                }

            }
        })
    }

//    override fun onCreateDelegateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
//        return super.onCreateViewHolder(parent, R.layout.buddy_list_item)
//    }
//
//    override fun onBindDelegateViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ViewType?) {
//        super.onBindViewHolder(holder as BuddyViewHolder, position)
//    }

//    override fun cleanUp() {
//        super.cleanup()
//    }

    class BuddyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}