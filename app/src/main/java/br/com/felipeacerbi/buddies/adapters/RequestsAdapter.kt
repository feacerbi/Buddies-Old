package br.com.felipeacerbi.buddies.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.Request
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.toFormatedDate
import com.firebase.ui.database.ChangeEventListener
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.request_list_item.view.*

class RequestsAdapter(val context: Context, requestsReference: DatabaseReference, val progressBar: ProgressBar) :
        FirebaseRecyclerAdapter<Boolean, RequestsAdapter.RequestViewHolder>
        (
                Boolean::class.java,
                R.layout.request_list_item,
                RequestViewHolder::class.java,
                requestsReference
        ) {

    companion object {
        val TAG = "RequestsAdapter"
    }

    val firebaseService = FirebaseService()

    override fun populateViewHolder(holder: RequestViewHolder?, item: Boolean?, position: Int) {
        val requestId = getRef(position).key

        firebaseService.getRequestReference(requestId).addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                Log.d(TAG, "Fail to retrieve request")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(dataSnapshot?.value != null && holder != null) {
                    val request = Request(dataSnapshot)

                    firebaseService.getUserReference(request.username).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {
                            Log.d(TAG, "Fail to retrieve user")
                        }

                        override fun onDataChange(userSnapshot: DataSnapshot?) {
                            if(userSnapshot?.value != null) {
                                val user = User(userSnapshot)

                                with(holder.itemView) {
                                    user_name.text = user.name

                                    Picasso.with(context)
                                            .load(user.photo)
                                            .placeholder(R.drawable.no_phototn)
                                            .error(R.drawable.no_phototn)
                                            .fit()
                                            .centerCrop()
                                            .into(requester_picture)
                                }
                            }
                        }
                    })

                    firebaseService.getPetReference(request.petId).addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {
                            Log.d(TAG, "Fail to retrieve buddy")
                        }

                        override fun onDataChange(petSnapshot: DataSnapshot?) {
                            if(petSnapshot?.value != null) {
                                val buddy = Buddy(petSnapshot)

                                with(holder.itemView) {
                                    pet_name.text = buddy.name
                                }
                            }
                        }
                    })

                    val reqTime = request.timestamp.toFormatedDate()

                    with(holder.itemView) {
                        timestamp.text = reqTime
                        allow_button.setOnClickListener { firebaseService.allowPetOwner(request, getRef(position).key, true) }
                        not_allow_button.setOnClickListener { firebaseService.allowPetOwner(request, getRef(position).key, false) }
                    }
                }
            }
        })
    }

    override fun onDataChanged() {
        super.onDataChanged()
        hideProgressBar()
    }

    override fun onChildChanged(type: ChangeEventListener.EventType?, snapshot: DataSnapshot?, index: Int, oldIndex: Int) {
        super.onChildChanged(type, snapshot, index, oldIndex)
        hideProgressBar()
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view)
}