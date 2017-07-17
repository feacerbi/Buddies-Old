package br.com.felipeacerbi.buddies.fragments

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.SettingsActivity
import br.com.felipeacerbi.buddies.adapters.BuddiesAdapter
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import kotlinx.android.synthetic.main.activity_main.*


/**
 * A fragment representing a list of Items.
 *
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class FirebaseListFragment : Fragment() {

    val firebaseService = FirebaseService()

    val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(activity)
    }

    val ref by lazy {
        firebaseService.getDatabaseReference(arguments.getString(DATABASE_REFERENCE))
    }

    companion object {
        val TAG = "FirebaseListFragment"
        val DATABASE_REFERENCE = "database_reference"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.buddies_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = BuddiesAdapter(ref)
            }
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        setUpFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))
    }

    override fun onResume() {
        super.onResume()
        setUpFab(sharedPreferences.getBoolean(SettingsActivity.QR_CODE_BUTTON_SHORTCUT_KEY, false))
    }

    fun setUpFab(show: Boolean) {
        if(show) {
            activity.fab?.visibility = View.VISIBLE
            activity.fab?.setImageDrawable(resources.getDrawable(R.drawable.ic_pets_white_24dp, activity.theme))
        } else {
            activity.fab?.visibility = View.GONE
        }
    }

//    fun getAdapter(): BuddiesAdapter? = if(list != null && list.adapter != null) (list.adapter as BuddiesAdapter) else null
}
