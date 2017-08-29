package br.com.felipeacerbi.buddies.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.fragments.PetsListFragment
import br.com.felipeacerbi.buddies.fragments.ProfileInfoFragment
import br.com.felipeacerbi.buddies.utils.create
import br.com.felipeacerbi.buddies.utils.makeQueryBundle

class ProfilePagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    val firebaseService = FirebaseService()

    companion object {
        val INFO_FRAGMENT = 0
        val BUDDIES_FRAGMENT = 1
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            INFO_FRAGMENT -> { return ProfileInfoFragment() }
            BUDDIES_FRAGMENT -> { return PetsListFragment()
                    .create(Bundle().makeQueryBundle(firebaseService.queryBuddies())) }
            else -> { return ProfileInfoFragment() }
        }
    }

    override fun getCount() = 2

}