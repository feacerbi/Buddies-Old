package br.com.felipeacerbi.buddies.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.inflate
import kotlinx.android.synthetic.main.buddy_list_check_item.view.*

class BuddiesMultiListAdapter(context: Context, val keys: Array<String>, val selectedKeys: Array<String>) : ArrayAdapter<Buddy>(context, R.layout.buddy_list_check_item) {

    companion object {
        val TAG = "BuddiesAdapter"
    }

    val firebaseService = FirebaseService()
    var tempKeys = arrayListOf<String>()

    init {
        tempKeys.addAll(selectedKeys)
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup): View {
        val convertView = view ?: viewGroup.inflate(R.layout.buddy_list_check_item)

        val buddy = getItem(position)

        if(buddy != null) {
            with(convertView) {
                name.text = buddy.name
                animal.text = buddy.animal
                breed.text = buddy.breed

                toggleSelection(buddy_check, position)

                if (buddy.photo.isNotEmpty()) {
                    com.squareup.picasso.Picasso.with(context)
                            .load(buddy.photo)
                            .placeholder(br.com.felipeacerbi.buddies.R.drawable.no_phototn)
                            .error(br.com.felipeacerbi.buddies.R.drawable.no_phototn)
                            .fit()
                            .centerCrop()
                            .into(picture)
                }

                setOnClickListener {
                    check(keys[position])
                    toggleSelection(buddy_check, position)
                }
            }
        }

        return convertView
    }

    fun toggleSelection(checkBox: CheckBox, position: Int) {
        checkBox.isChecked = tempKeys.contains(keys[position])
    }

    fun check(key: String) {
        if(tempKeys.contains(key)) {
            unselect(key)
        } else {
            select(key)
        }
    }

    fun select(key: String) {
        tempKeys.add(key)
    }

    fun unselect(key: String) {
        tempKeys.remove(key)
    }

    fun getSelectedKeys(): Map<String, Boolean> {
        val keysMap = HashMap<String, Boolean>()
        tempKeys.forEach {
            keysMap.put(it, true)
        }

        return keysMap
    }
}