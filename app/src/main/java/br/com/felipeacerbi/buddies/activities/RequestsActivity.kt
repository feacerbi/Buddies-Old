package br.com.felipeacerbi.buddies.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.fragments.RequestsListFragment
import br.com.felipeacerbi.buddies.utils.makeQueryBundle
import br.com.felipeacerbi.buddies.utils.transact
import kotlinx.android.synthetic.main.activity_requests.*

class RequestsActivity : AppCompatActivity() {

    val firebaseService = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        RequestsListFragment().transact(
                this,
                container.id,
                Bundle().makeQueryBundle(firebaseService.queryRequests())
        )
    }
}
