package br.com.felipeacerbi.buddies.fragments

import android.Manifest
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.BuddyProfileActivity
import br.com.felipeacerbi.buddies.activities.QRCodeActivity
import br.com.felipeacerbi.buddies.activities.SettingsActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesAdapter
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.buddies_list.*
import kotlinx.android.synthetic.main.buddies_list.view.*


/**
 * A fragment representing a list of Items.
 *
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class PetsListFragment : Fragment(), IListClickListener {

    val firebaseService = FirebaseService()

    val permissionsManager: PermissionsManager by lazy {
        PermissionsManager(activity)
    }

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    val ref by lazy {
        firebaseService.getDatabaseReference(arguments.getString(DATABASE_REFERENCE))
    }

    companion object {
        val TAG = "PetsListFragment"
        val DATABASE_REFERENCE = "database_reference"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.buddies_list, container, false)

        // Set the adapter
        if(view is RelativeLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager(context)
                list.adapter = BuddiesAdapter(this@PetsListFragment, ref, firebaseService.getPetsReference(), progress)
            }
        }

        return view
    }

    override fun onListClick(identifiers: Array<Any>?) {
        activity.launchActivityWithExtras(
                BuddyProfileActivity::class,
                arrayOf(BuddyProfileActivity.EXTRA_PETID,
                        BuddyProfileActivity.EXTRA_EDITABLE),
                identifiers)
    }

    override fun selectListItem(position: Int) {
        list?.scrollToPosition(position)
    }

    override fun onResume() {
        super.onResume()
        setUpFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))
    }

    open fun setUpFab(show: Boolean) {
        activity.fab?.setUp(activity, show, R.drawable.ic_add_a_photo_white_24dp) {
            permissionsManager.actionWithPermission(Manifest.permission.CAMERA) { activity.launchActivity(QRCodeActivity::class) }
        }
    }

    fun cleanUp() {
        list?.getFirebaseAdapter()?.cleanup()
    }

    override fun getViewInflater() = activity.layoutInflater
}
