package br.com.felipeacerbi.buddies.fragments

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.RequestsAdapter
import kotlinx.android.synthetic.main.requests_list.view.*

/**
 * A fragment representing a list of Items.
 *
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
open class RequestsListFragment : PetsListFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.requests_list, container, false)

        // Set the adapter
        if(view is ConstraintLayout) {
            with(view) {
                list.layoutManager = LinearLayoutManager (context)
                list.adapter = RequestsAdapter(activity, ref, firebaseService.getRequestsReference(), progress)
            }
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        setUpFab(false)
    }

    override fun onResume() {
        super.onResume()
        setUpFab(false)
    }
}
