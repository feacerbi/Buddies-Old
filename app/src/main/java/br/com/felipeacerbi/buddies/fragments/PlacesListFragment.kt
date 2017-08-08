package br.com.felipeacerbi.buddies.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.PlaceActivity
import br.com.felipeacerbi.buddies.adapters.PlacesAdapter
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.FriendlyItem
import br.com.felipeacerbi.buddies.models.Place
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.setUp
import br.com.felipeacerbi.buddies.utils.showInputDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.friendly_item_check.view.*
import kotlinx.android.synthetic.main.places_list.view.*
import kotlinx.android.synthetic.main.suggest_place_dialog.view.*
import android.support.v4.app.ShareCompat.IntentBuilder
import com.google.android.gms.location.places.ui.PlacePicker
import android.widget.Toast
import android.R.attr.data
import android.app.Activity


/**
 * A fragment representing a list of Items.
 *
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class PlacesListFragment : PetsListFragment() {

    companion object {
        val PLACE_PICKER_REQUEST = 100
    }

    val placesFab by lazy {
        activity.fab
    }

    val suggestionView by lazy {
        activity.layoutInflater.inflate(R.layout.suggest_place_dialog, null)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.places_list, container, false)

        // Set the adapter
        if(view is RelativeLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager (context)
                list.adapter = PlacesAdapter(this@PlacesListFragment, ref, progress)
                list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            placesFab?.hide()
                            placesFab?.isClickable = false
                        } else {
                            placesFab?.show()
                            placesFab?.isClickable = true
                        }
                    }
                })
            }
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        setUpFab(true)
    }

    override fun onResume() {
        super.onResume()
        setUpFab(true)
    }

    override fun setUpFab(show: Boolean) {
        placesFab?.setUp(activity, show, R.drawable.ic_add_location_white_24dp) {

            firebaseService.getFriendlyItemsReference().addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Fail to retrieve item")
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if(dataSnapshot?.value != null && dataSnapshot.hasChildren()) {
                        suggestionView.items_list.removeAllViews()
                        dataSnapshot.children.forEach {
                            suggestionView.items_list.addView(createFriendlyView(it))
                        }
                    }
                }
            })

            suggestionView.place_in_map_button.setOnClickListener {
                val builder = PlacePicker.IntentBuilder()
                startActivityForResult(builder.build(activity), PLACE_PICKER_REQUEST)
            }

            AlertDialog.Builder(activity).showInputDialog(
                    "Suggest a place",
                    "Send",
                    suggestionView,
                    { _, _ ->
                        with(suggestionView) {
                            val newPlace = Place(
                                    name = place_name.text.toString(),
                                    address = place_address.text.toString(),
                                    phone = place_phone.text.toString(),
                                    website = place_website.text.toString(),
                                    description = place_description.text.toString(),
                                    category = place_category.selectedItem as String,
                                    items = getSelectedItems(items_list))
                            firebaseService.addPlace(newPlace)
                        }
                    })

        }
    }

    private fun  createFriendlyView(dataSnapshot: DataSnapshot?): View? {
        val view = activity.layoutInflater.inflate(R.layout.friendly_item_check, null, false)
        val friendlyItem = FriendlyItem(dataSnapshot)

        view.item_name.text = friendlyItem.name
        view.setTag(R.integer.friendly_item_id_tag, dataSnapshot?.key)

        return view
    }

    fun getSelectedItems(linearLayout: LinearLayout): Map<String, Boolean> {
        var i = 0
        val list = HashMap<String, Boolean>(linearLayout.childCount)

        while(i < linearLayout.childCount) {
            val check = linearLayout.getChildAt(i) as AppCompatCheckBox
            if(check.isChecked) {
                list.put(check.getTag(R.integer.friendly_item_id_tag) as String, true)
            }
            i++
        }
        return list
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                PLACE_PICKER_REQUEST -> {
                    val place = PlacePicker.getPlace(activity, data)
                    suggestionView.place_address.setText(place.address.toString())
                    suggestionView.place_name.setText(place.name)
                    suggestionView.place_phone.setText(place.phoneNumber)
                    suggestionView.place_website.setText(place.websiteUri.toString())
                    suggestionView.place_description.setText(place.attributions)
                }
            }
        }
    }

    override fun onListClick(identifiers: Array<Any>?) {
        activity.launchActivityWithExtras(
                PlaceActivity::class,
                arrayOf(PlaceActivity.EXTRA_PLACEID),
                identifiers)
    }
}
