package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.PetProfilePagerAdapter
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.fragments.PostsListFragment
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_pet_profile.*

class BuddyProfileActivity : FireListener() {

    companion object {
        val TAG = "BuddyProfileActivity"
        val EXTRA_PETID = "extra_petid"
        val EXTRA_EDITABLE = "extra_editable"
    }

    var petId = ""
    var editable = false
    var buddyReference: DatabaseReference? = null
    var petProfilePagerAdapter: PetProfilePagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

        handleIntent(intent)
        setUpUI()
    }

    fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val reference = buddyReference
        if(reference != null) {
            petProfilePagerAdapter = PetProfilePagerAdapter(supportFragmentManager, reference)
        }

        container.adapter = petProfilePagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            petId = intent.extras.getString(EXTRA_PETID)
            editable = intent.extras.getString(EXTRA_EDITABLE).toBoolean()
            buddyReference = firebaseService.getPetReference(petId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFragmentManager.fragments?.forEach {
            if(it is PostsListFragment) {
                it.cleanUp()
            }
        }
    }
}