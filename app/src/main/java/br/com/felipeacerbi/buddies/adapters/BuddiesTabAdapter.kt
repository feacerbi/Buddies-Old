package br.com.felipeacerbi.buddies.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.BuddyProfileActivity
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.buddy_list_item.view.*

class BuddiesTabAdapter(
        val listener: IListClickListener,
        val userPetsReference: DatabaseReference,
        val petsReference: DatabaseReference,
        val progressBar: ProgressBar) :
        FirebaseIndexRecyclerAdapter<Buddy, BuddiesTabAdapter.BuddyViewHolder>
        (
                Buddy::class.java,
                R.layout.buddy_list_item,
                BuddyViewHolder::class.java,
                userPetsReference,
                petsReference
        ) {

    companion object {
        val TAG = "BuddiesAdapter"
        val HEADER_FOOTER_VIEW_TYPE = 1
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: BuddyViewHolder, buddy: Buddy, position: Int) {
        if(buddy.animal.isNotEmpty()) {
            val petId = getRef(position).key

            with(holder.itemView) {
                name.text = buddy.name
                animal.text = buddy.animal
                breed.text = buddy.breed

                if (buddy.photo.isNotEmpty()) {
                    Picasso.with(listener.getContext())
                            .load(buddy.photo)
                            .placeholder(R.drawable.no_phototn)
                            .error(R.drawable.no_phototn)
                            .fit()
                            .centerCrop()
                            .into(picture)
                }

                remove_button.setOnClickListener { firebaseService.removePetFromUser(userPetsReference.key, petId) }
                click_profile_layout.setOnClickListener {
                    listener.onListClick<BuddyProfileActivity>(
                            BuddyProfileActivity::class,
                            arrayOf(BuddyProfileActivity.EXTRA_PETID),
                            arrayOf(petId))
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
        listener.selectListItem(0)
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BuddyViewHolder {
        if(viewType == HEADER_FOOTER_VIEW_TYPE) {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.header_footer, parent, false)
            view.setBackgroundColor(listener.getContext().resources.getColor(android.R.color.white))
            return HeaderFooterViewHolder(view)
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        if(position == itemCount - 1) return HEADER_FOOTER_VIEW_TYPE
        return super.getItemViewType(position)
    }

    override fun getItem(position: Int): Buddy {
        if(itemCount > 1 && position < itemCount - 1) {
            return super.getItem(position)
        } else {
            return Buddy()
        }
    }

    override fun getRef(position: Int): DatabaseReference {
        if(itemCount > 1 && position < itemCount - 1) {
            return super.getRef(position)
        }
        return firebaseService.getDatabaseReference("")
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    open class BuddyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class HeaderFooterViewHolder(view: View) : BuddyViewHolder(view)
}