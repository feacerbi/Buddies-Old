package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.utils.getFirebaseAdapter
import br.com.felipeacerbi.buddies.utils.launchActivity
import br.com.felipeacerbi.buddies.utils.showInputDialog
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pet_profile.*
import kotlinx.android.synthetic.main.input_dialog.view.*

class BuddyProfileActivity : FireListener() {

    companion object {
        val TAG = "BuddyProfileActivity"
        val EXTRA_PETID = "extra_petid"
    }

    val fireBuilder by lazy {
        FireBuilder()
    }

    var buddy: Buddy? = null
    var photoUrl: String? = ""
    var petId = ""

    var buddyReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_profile)

        setUpUI()
        handleIntent(intent)
    }

    fun setUpUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(posts_list) {
            layoutManager = LinearLayoutManager(context)
//            adapter =
        }

        buddy_name_edit_button.setOnClickListener {
            showEditDialog("Edit name", buddy_name.text.toString(), { buddy?.name = it })
        }

        buddy_breed_edit_button.setOnClickListener {
            showEditDialog("Edit breed", buddy_breed.text.toString(), { buddy?.breed = it })
        }
    }

    fun showEditDialog(title: String, editValue: String, setFunc: (String) -> Unit) {
        val inputView = layoutInflater.inflate(R.layout.input_dialog, null)
        with(inputView) {
            input_field.setText(editValue)

            AlertDialog.Builder(context).showInputDialog(
                    title,
                    "OK",
                    this,
                    { _, _ ->
                        val newValue = input_field.text.toString()
                        setFunc(newValue)
                        firebaseService.updatePet(buddy, petId)
                    }
            )
        }
    }

    override fun onResume() {
        super.onResume()

        fireBuilder.onRef(buddyReference)
                .mode(MODE_CONTINUOUS)
                .complete {
                    if(it != null && it.hasChildren()) {
                        buddy = Buddy(it)
                        buddy_name.text = buddy?.name
                        actionBar?.title = buddy?.name
                        buddy_breed.text = buddy?.breed
                        photoUrl = buddy?.photo

                        Picasso.with(this)
                                .load(photoUrl)
                                .error(R.mipmap.ic_launcher_round)
                                .resize(500, 500)
                                .centerCrop()
                                .into(picture)
                    }
                }
                .cancel { Log.d(TAG, "Buddy not found") }
                .listen()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_default_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            android.R.id.home -> finish()
            R.id.action_settings -> launchActivity(SettingsActivity::class)
            R.id.action_requests -> launchActivity(RequestsActivity::class)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            petId = intent.extras.getString(EXTRA_PETID)
            buddyReference = firebaseService.getPetReference(petId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        posts_list.getFirebaseAdapter()?.cleanup()
    }
}
