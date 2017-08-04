package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_place.*
import kotlinx.android.synthetic.main.content_place.*


class PlaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)
        setSupportActionBar(toolbar)

        toolbar_layout.title = "The Sailor Burger & Beer"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        maps_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("geo:0,0?q=" + "R. Vupabussu, 309 - Pinheiros, São Paulo - SP, 05429-040")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        Picasso.with(this)
                .load("https://firebasestorage.googleapis.com/v0/b/buddies-5d07f.appspot.com/o/places%2F-KqbvwNZUSPM_Rwqf3G2%2Fimages%20(1).jpg?alt=media&token=0b06c7aa-c5ae-4a7c-9113-7c7c82aabff1")
                .placeholder(R.drawable.no_phototn)
                .error(R.drawable.no_phototn)
                .fit()
                .centerCrop()
                .into(place_photo)
    }
}
