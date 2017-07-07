package br.com.felipeacerbi.buddies.adapters.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewTypeDelegateAdapter
import br.com.felipeacerbi.buddies.utils.inflate
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.buddy_list_item.view.*

/**
 * Created by felipe.acerbi on 04/07/2017.
 */

class BuddyDelegateAdapter(val petsReference: DatabaseReference) : ViewTypeDelegateAdapter,
        FirebaseRecyclerAdapter<Buddy, BuddyDelegateAdapter.BuddyViewHolder>
        (
                Buddy::class.java,
                R.layout.buddy_list_item,
                BuddyViewHolder::class.java,
                petsReference
) {

    override fun populateViewHolder(holder: BuddyViewHolder?, item: Buddy?, position: Int) {
        TODO()
        val petId = getRef(position).key

        if(holder != null && item != null) {
            with(holder.itemView) {
                name.text = item. name
                breed.text = item.breed
                tagID.text = item.tag
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return BuddyViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        holder as BuddyViewHolder
        item as Buddy

        populateViewHolder(holder, item, 0)
    }

    override fun cleanUp() {
        super.cleanup()
        cleanup()
    }

    inner class BuddyViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.buddy_list_item))

}