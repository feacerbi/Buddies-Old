package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.FriendlyItem
import br.com.felipeacerbi.buddies.models.Place
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_place.*
import kotlinx.android.synthetic.main.content_place.*
import kotlinx.android.synthetic.main.friendly_item_icon.view.*


class PlaceActivity : FireListener() {

    companion object {
        val TAG = "PlaceActivity"
        val EXTRA_PLACEID = "extra_placeid"
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    var place: Place? = null
    var placeId = ""

    var placeReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        setUpUI()

        fireBuilder.onRef(placeReference)
                .mode(MODE_SINGLE)
                .complete {
                    if(it != null && it.hasChildren()) {
                        place = Place(it)
                        toolbar_layout.title = place?.name
                        address_text.text = place?.address
                        place_description.text = place?.description
                        phone_text.text = place?.phone
                        website_text.text = place?.website

                        maps_button.setOnClickListener {
                            openMapAddress(place?.address)
                        }
                        phone_button.setOnClickListener {
                            openCall(place?.phone)
                        }
                        website_button.setOnClickListener {
                            Log.d(TAG, "Opening")
                            openWebsite(place?.website)
                        }

                        fireBuilder.onRef(firebaseService.getPlaceFriendlyItemsReference(it.key))
                                .mode(MODE_SINGLE)
                                .complete {
                                    if(it?.value != null && it.hasChildren()) {
                                        friendly_items.removeAllViews()
                                        it.children.forEach {
                                            friendly_items.addView(createFriendlyView(it))
                                        }
                                        progress.progress = place?.calcRating(it.childrenCount) ?: 0
                                        progress_number.text = place?.getRatingGrade(it.childrenCount)
                                    }
                                }
                                .cancel { Log.d(TAG, "Friendly item not found") }
                                .listen()

                        val placePhoto = place?.photo
                        if(placePhoto != null && placePhoto.isNotEmpty()) {
                            Picasso.with(this)
                                    .load(place?.photo)
                                    .placeholder(R.drawable.no_phototn)
                                    .error(R.drawable.no_phototn)
                                    .fit()
                                    .centerCrop()
                                    .into(place_photo)
                        }
                    }
                }
                .cancel { Log.d(TAG, "Buddy not found") }
                .listen()
    }

    fun createFriendlyView(dataSnapshot: DataSnapshot): View {
        val view = layoutInflater.inflate(R.layout.friendly_item_icon, null, false)

        firebaseService.getFriendlyItemReference(dataSnapshot.key).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve friendly item")
            }

            override fun onDataChange(itemDataSnapshot: DataSnapshot?) {
                if(itemDataSnapshot?.value != null && itemDataSnapshot.hasChildren()) {
                    val item = FriendlyItem(itemDataSnapshot)
                    with(view as LinearLayout) {
                        item_icon.setImageResource(item.getIconFromType())
                        item_text.text = item.name
                    }
                }
            }
        })

        return view
    }

    fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    fun openMapAddress(address: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("geo:0,0?q=" + address)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    fun openCall(number: String?) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:" + number)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    fun openWebsite(website: String?) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(website)
        if (intent.resolveActivity(packageManager) != null) {
            Log.d(TAG, "Starting")
            startActivity(intent)
        }
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            placeId = intent.extras.getString(EXTRA_PLACEID)
            placeReference = firebaseService.getPlaceReference(placeId)
        }
    }
}
