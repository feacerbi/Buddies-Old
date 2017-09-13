package br.com.felipeacerbi.buddies.fragments

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.TabLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.ProfilePagerAdapter
import kotlinx.android.synthetic.main.profile_fragment.view.*

class ProfileFragment : PetsListFragment() {

    companion object {
        val TAG = "ProfileFragment"
    }

    var profilePagerAdapter: ProfilePagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.profile_fragment, container, false)

        profilePagerAdapter = ProfilePagerAdapter(childFragmentManager)

        if(view is CoordinatorLayout) {
            with(view) {
                parentActivity.setSupportActionBar(toolbar)
                view_pager.adapter = profilePagerAdapter
                view_pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
                tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(view_pager))
            }
        }

        return view
    }
}
