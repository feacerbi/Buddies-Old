package br.com.felipeacerbi.buddies.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.CommentsAdapter
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Comment
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.activity_comments.*

class CommentsActivity : AppCompatActivity() {

    companion object {
        val POST_ID_EXTRA = "post_id"
    }

    val firebaseService = FirebaseService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        handleIntent(intent)
        setUpUI()
    }

    var postId: String? = null
    var postCommentsReference: DatabaseReference? = null

    private fun handleIntent(intent: Intent) {
        postId = intent.getStringExtra(POST_ID_EXTRA)

        val id = postId
        if(id != null) {
            postCommentsReference = firebaseService.getPostCommentsReference(id)
        }
    }

    private fun setUpUI() {
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val ref = postCommentsReference
        if(ref != null) {
            comments_list.layoutManager = LinearLayoutManager(this)
            comments_list.adapter = CommentsAdapter(this, ref, firebaseService.getCommentsReference(), progress)
        }

        send_icon.setOnClickListener {
            val newComment = createComment()
            if(newComment != null) firebaseService.addComment(newComment)
            send_message.setText("")
        }

        send_message.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                // No need
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // No need
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, size: Int) {
                send_icon.isEnabled = size != 0
            }
        })
    }

    fun createComment(): Comment? {
        val id = postId

        if(id != null) {
            return Comment(postId = id, message = send_message.text.toString())
        }

        return null
    }

}
