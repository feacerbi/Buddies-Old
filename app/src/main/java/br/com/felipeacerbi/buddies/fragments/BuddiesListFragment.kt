package br.com.felipeacerbi.buddies.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.BuddiesApplication
import br.com.felipeacerbi.buddies.FirebaseService
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.delegates.BuddiesAdapter
import br.com.felipeacerbi.buddies.adapters.interfaces.IOnListFragmentInteractionListener
import kotlinx.android.synthetic.main.buddies_list.*

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [IOnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class BuddiesListFragment : Fragment() {

    private var mListener: IOnListFragmentInteractionListener? = null
    val firebaseService: FirebaseService = FirebaseService()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.buddies_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = BuddiesAdapter(firebaseService.getUserPetsReference(firebaseService.getCurrentUsername()), mListener)
            }
        }
        return view
    }

    fun getAdapter(): BuddiesAdapter? = if(list != null && list.adapter != null) (list.adapter as BuddiesAdapter) else null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is IOnListFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
}
