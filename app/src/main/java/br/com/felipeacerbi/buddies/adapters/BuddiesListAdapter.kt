package br.com.felipeacerbi.buddies.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.inflate
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.buddy_list_item.view.*

class BuddiesListAdapter(context: Context) : ArrayAdapter<Buddy>(context, R.layout.buddy_list_item) {

    companion object {
        val TAG = "BuddiesAdapter"
    }

    val firebaseService = FirebaseService()

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val convertView = view ?: viewGroup.inflate(R.layout.buddy_list_item)

        val buddy = getItem(position)

        if(buddy != null) {
            with(convertView) {
                name.text = buddy.name
                animal.text = buddy.animal
                breed.text = buddy.breed
                remove_button.visibility = View.GONE

                if (buddy.photo.isNotEmpty()) {
                    Picasso.with(context)
                            .load(buddy.photo)
                            .placeholder(R.drawable.no_phototn)
                            .error(R.drawable.no_phototn)
                            .fit()
                            .centerCrop()
                            .into(picture)
                }
            }
        }

        return convertView
    }
}