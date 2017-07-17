package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.models.BuddyInfo
import br.com.felipeacerbi.buddies.nfc.tags.BaseTag
import kotlinx.android.synthetic.main.activity_new_pet.*

class NewPetActivity : AppCompatActivity() {

    companion object {
        val BUDDY_INFO_EXTRA = "buddy_info"
        val EXTRA_BASETAG = "basetag"
    }

    var baseTag: BaseTag? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_pet)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        handleIntent(intent)

        val resultIntent = Intent(this, MainActivity::class.java)

        cancel_button.setOnClickListener {
            setResult(RESULT_CANCELED, resultIntent)
            finish()
        }
        add_button.setOnClickListener {
            val name = pet_name.text.toString()
            val breed = breed.text.toString()

            resultIntent.putExtra(BUDDY_INFO_EXTRA, BuddyInfo(name, breed))
            resultIntent.putExtra(EXTRA_BASETAG, baseTag)
            setResult(RESULT_OK, resultIntent)

            finish()
        }
    }

    private fun  handleIntent(intent: Intent?) {
        if(intent != null) {
            baseTag = intent.extras.getSerializable(EXTRA_BASETAG) as BaseTag
        }
    }


}
