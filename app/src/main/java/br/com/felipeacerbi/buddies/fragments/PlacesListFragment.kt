package br.com.felipeacerbi.buddies.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.SuggestPlaceActivity
import br.com.felipeacerbi.buddies.adapters.PlacesAdapter
import br.com.felipeacerbi.buddies.utils.launchActivityForResult
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.setUp
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.places_list.*
import kotlinx.android.synthetic.main.places_list.view.*
import kotlin.reflect.KClass


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
        val SUGGEST_PLACE = 300
    }

    val placesFab by lazy {
        activity.fab
    }

    val locationProvider: FusedLocationProviderClient  by lazy {
        LocationServices.getFusedLocationProviderClient(activity)
    }

    val locationCallback: LocationCallback by lazy {
        object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if(locationResult?.lastLocation != null) {
                    firebaseService.registerUserLocation(locationResult.lastLocation)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.places_list, container, false)

        // Set the adapter
        if(view is RelativeLayout) {
            with(view) {
                parentActivity.setSupportActionBar(toolbar)
                list.layoutManager = LinearLayoutManager (context)
                list.adapter = PlacesAdapter(this@PlacesListFragment, ref.orderByValue(), progress)
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

    override fun onResume() {
        super.onResume()
        setUpFab(true)
        getUserLocation()
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission") // Handled by Permissions Manager
    fun getUserLocation() {
        permissionsManager.actionWithPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            locationProvider.lastLocation.addOnSuccessListener {
                location ->
                firebaseService.registerUserLocation(location)
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("MissingPermission") // Handled by Permissions Manager
    fun startLocationUpdates() {
        permissionsManager.actionWithPermission(Manifest.permission.ACCESS_FINE_LOCATION) {
            locationProvider.requestLocationUpdates(
                    createLocationRequest(),
                    locationCallback,
                    null)
        }
    }

    fun stopLocationUpdates() {
        locationProvider.removeLocationUpdates(locationCallback)
    }

    fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 300000 // 5 minutes
        locationRequest.fastestInterval = 60000 // 1 minute
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    override fun setUpFab(show: Boolean) {
        placesFab?.setUp(activity, show, R.drawable.ic_add_location_white_24dp) {
            activity.launchActivityForResult(SuggestPlaceActivity::class, SUGGEST_PLACE)
        }
    }

    override fun <T : Any> onListClick(clazz: KClass<T>, identifiers: Array<String>?, extras: Array<Any>?) {
        activity.launchActivityWithExtras(
                clazz,
                identifiers,
                extras)
    }

    override fun selectListItem(position: Int) {
        list?.scrollToPosition(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "Activity request code " + requestCode)

        when(requestCode) {
            SUGGEST_PLACE -> {
                when(resultCode) {
                    Activity.RESULT_OK -> { Toast.makeText(activity, "Thank you for the suggestion!", Toast.LENGTH_SHORT).show() }
                    Activity.RESULT_CANCELED -> { Toast.makeText(activity, "Maybe another time...", Toast.LENGTH_SHORT).show() }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
