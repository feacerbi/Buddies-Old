package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.buddy_list_item.view.*

class BuddiesAdapter(
        val listener: IListClickListener,
        val userPetsReference: DatabaseReference,
        val petsReference: DatabaseReference,
        val progressBar: ProgressBar) :
        FirebaseIndexRecyclerAdapter<Buddy, BuddiesAdapter.BuddyViewHolder>
        (
                Buddy::class.java,
                R.layout.buddy_list_item,
                BuddyViewHolder::class.java,
                userPetsReference,
                petsReference
        ) {

    companion object {
        val TAG = "BuddiesAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: BuddyViewHolder, buddy: Buddy, position: Int) {
        Log.d(TAG, "Load buddy " + buddy.name)
        val petId = getRef(position).key

        with(holder.itemView) {
            name.text = buddy.name
            animal.text = buddy.animal
            breed.text = buddy.breed
//                        followers.text = buddy.followers.size.toString()
//                        followers_text.text = if(buddy.followers.size == 1) " follower" else " followers"

            if(buddy.photo.isNotEmpty()) {
                Picasso.with(listener.getContext())
                        .load(buddy.photo)
                        .placeholder(R.drawable.no_phototn)
                        .error(R.drawable.no_phototn)
                        .fit()
                        .centerCrop()
                        .into(picture)
            }

            val editable = petsReference.key == Buddy.DATABASE_OWNS_CHILD

            remove_button.setOnClickListener { firebaseService.removePetFromUser(petsReference.key, petId) }
            click_profile_layout.setOnClickListener { listener.onListClick(arrayOf(petId, editable)) }
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

    class BuddyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}