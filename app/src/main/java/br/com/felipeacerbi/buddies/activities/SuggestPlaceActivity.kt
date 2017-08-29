package br.com.felipeacerbi.buddies.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatCheckBox
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.FriendlyItem
import br.com.felipeacerbi.buddies.models.Place
import br.com.felipeacerbi.buddies.utils.toFormatedWebsite
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.firebase.database.DataSnapshot
import kotlinx.android.synthetic.main.activity_suggest_place.*
import kotlinx.android.synthetic.main.friendly_item_check.view.*

class SuggestPlaceActivity : FireListener() {

    companion object {
        val TAG = "SuggestPlaceActivity"
        val PLACE_PICKER_REQUEST = 300
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suggest_place)
    }

    var mapsPlace: com.google.android.gms.location.places.Place? = null

    override fun onResume() {
        super.onResume()
        setUpUI()

        fireBuilder.onRef(firebaseService.getFriendlyItemsReference())
                .mode(MODE_SINGLE)
                .complete {
                    if(it?.value != null && it.hasChildren()) {
                        items_list.removeAllViews()
                        it.children.forEach {
                            items_list.addView(createFriendlyView(it))
                        }
                    }
                }
                .cancel { Log.d(TAG, "Friendly item not found") }
                .listen()
    }

    fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        place_in_map_button.setOnClickListener {
            openMapView()
        }

        cancel_button.setOnClickListener {
            onBackPressed()
        }

        add_button.setOnClickListener {
            val newPlace = Place(
                    name = place_name.text.toString(),
                    address = place_address.text.toString(),
                    phone = place_phone.text.toString(),
                    website = place_website.text.toString(),
                    description = place_description.text.toString(),
                    category = place_category.selectedItem as String,
                    items = getSelectedItems(items_list))
            newPlace.setLatLongPosition(mapsPlace)

            firebaseService.addSuggestion(newPlace)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun openMapView() {
        val builder = PlacePicker.IntentBuilder()
        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
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

    private fun  createFriendlyView(dataSnapshot: DataSnapshot?): View? {
        val view = layoutInflater.inflate(R.layout.friendly_item_check, null, false)
        val friendlyItem = FriendlyItem(dataSnapshot)

        view.item_name.text = friendlyItem.name
        view.setTag(R.integer.friendly_item_id_tag, dataSnapshot?.key)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PLACE_PICKER_REQUEST) {
            when(resultCode) {
                Activity.RESULT_OK -> {
                    val place = PlacePicker.getPlace(this, data)
                    place_address.setText(place.address.toString())
                    place_name.setText(place.name)
                    place_phone.setText(place.phoneNumber)
                    place_website.setText(place.websiteUri?.toString()?.toFormatedWebsite())
                    mapsPlace = place
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
