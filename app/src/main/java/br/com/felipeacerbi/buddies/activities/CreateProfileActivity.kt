package br.com.felipeacerbi.buddies.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.launchActivityAndFinish
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*

class CreateProfileActivity : AppCompatActivity() {

    val  firebaseService = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        name.setText(firebaseService.getCurrentUserDisplayName())
        Picasso.with(this)
                .load(firebaseService.getCurrentUserPicture())
                .error(R.mipmap.ic_launcher)
                .resize(400, 400)
                .centerCrop()
                .into(profile_picture)

        cancel_button.setOnClickListener {
            launchActivityAndFinish(LoginActivity::class)
        }

        save_button.setOnClickListener {
            val user = User(name = name.text.toString())

            firebaseService.registerUser(user)
            launchActivityAndFinish(MainActivity::class)
        }
    }
}
