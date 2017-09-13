package br.com.felipeacerbi.buddies.fragments

import android.Manifest
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.QRCodeActivity
import br.com.felipeacerbi.buddies.activities.SettingsActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesAdapter
import br.com.felipeacerbi.buddies.adapters.BuddiesTabAdapter
import br.com.felipeacerbi.buddies.adapters.listeners.IListClickListener
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.buddies_list.*
import kotlinx.android.synthetic.main.buddies_list.view.*
import kotlin.reflect.KClass


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

    val parentActivity by lazy {
        activity as AppCompatActivity
    }

    companion object {
        val TAG = "PetsListFragment"
        val DATABASE_REFERENCE = "database_reference"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val tab = container is ViewPager

        var view = inflater?.inflate(R.layout.buddies_list, container, false)
        if(tab) {
            view = inflater?.inflate(R.layout.tab_buddies_list, container, false)
        } else {
            parentActivity.setSupportActionBar(view?.toolbar)
        }

        // Set the adapter
        if(view is RelativeLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager(context)
                if(tab) {
                    list.adapter = BuddiesTabAdapter(this@PetsListFragment, ref, firebaseService.getPetsReference(), progress)
                } else {
                    list.adapter = BuddiesAdapter(this@PetsListFragment, ref, firebaseService.getPetsReference(), progress)
                }
            }
        }

        return view
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
