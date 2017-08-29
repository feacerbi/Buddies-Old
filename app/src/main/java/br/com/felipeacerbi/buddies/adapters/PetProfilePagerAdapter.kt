package br.com.felipeacerbi.buddies.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.fragments.PetProfileInfoFragment
import br.com.felipeacerbi.buddies.fragments.PostsListFragment
import br.com.felipeacerbi.buddies.fragments.ProfileInfoFragment
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.create
import br.com.felipeacerbi.buddies.utils.makeQueryBundle
import com.google.firebase.database.DatabaseReference

class PetProfilePagerAdapter(
        fm: FragmentManager,
        val ref: DatabaseReference) : FragmentPagerAdapter(fm) {

    val firebaseService = FirebaseService()

    companion object {
        val INFO_FRAGMENT = 0
        val POSTS_FRAGMENT = 1
    }

    override fun getItem(position: Int): Fragment {
        when(position) {
            INFO_FRAGMENT -> {
                return PetProfileInfoFragment().create(Bundle().makeQueryBundle(ref))
            }
            POSTS_FRAGMENT -> {
                return PostsListFragment().create(Bundle().makeQueryBundle(ref.child(Buddy.DATABASE_POSTS_CHILD)))
            }
            else -> { return ProfileInfoFragment()
            }
        }
    }

    override fun getCount() = 2

}