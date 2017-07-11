package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.models.BaseTag
import br.com.felipeacerbi.buddies.models.BuddyInfo
import kotlinx.android.synthetic.main.activity_new_pet.*

class NewPetActivity : AppCompatActivity() {

    companion object {
        val BUDDY_INFO_EXTRA = "buddy_info"
        val EXTRA_BASETAG = "basetag"
        val RESULT_OK = 0
    }

    var baseTag: BaseTag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_pet)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleIntent(intent)

        cancel_button.setOnClickListener { finish() }
        add_button.setOnClickListener {
            val name = pet_name.text.toString()
            val breed = breed.text.toString()

            val petInfoIntent = Intent(this, MainActivity::class.java)
            petInfoIntent.putExtra(BUDDY_INFO_EXTRA, BuddyInfo(name, breed))
            petInfoIntent.putExtra(EXTRA_BASETAG, baseTag)
            setResult(RESULT_OK, petInfoIntent)

            finish()
        }
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            baseTag = intent.extras.getSerializable(EXTRA_BASETAG) as BaseTag
        }
    }


}