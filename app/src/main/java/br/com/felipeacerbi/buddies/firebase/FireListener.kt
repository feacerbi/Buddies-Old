package br.com.felipeacerbi.buddies.firebase

import br.com.felipeacerbi.buddies.activities.base.RxBaseActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

abstract class FireListener : RxBaseActivity() {

    companion object {
        val MODE_SINGLE = 0
        val MODE_CONTINUOUS = 1
    }

    val firebaseService = FirebaseService()

    var listenerMap = HashMap<DatabaseReference, ValueEventListener>()

    override fun onResume() {
        super.onResume()
        listenerMap = HashMap<DatabaseReference, ValueEventListener>()
    }

    override fun onPause() {
        super.onPause()
        listenerMap.forEach { it.key.removeEventListener(it.value) }
    }

    inner class FireBuilder(
        private var reference: DatabaseReference? = null,
        private var completed: (dataSnapshot: DataSnapshot?) -> Unit = { _ -> },
        private var canceled: (dataSnapshot: DatabaseError?) -> Unit = { _ -> },
        private var mode: Int = MODE_CONTINUOUS) {

        fun onRef(dataReference: DatabaseReference?): FireBuilder {
            reference = dataReference
            return this
        }

        fun complete(action: (dataSnapshot: DataSnapshot?) -> Unit): FireBuilder {
            completed = action
            return this
        }

        fun cancel(action: (dataSnapshot: DatabaseError?) -> Unit): FireBuilder {
            canceled = action
            return this
        }

        fun mode(eventMode: Int): FireBuilder {
            mode = eventMode
            return this
        }

        fun listen() {
            val eventListener = object : ValueEventListener {
                override fun onCancelled(dataSnapshot: DatabaseError?) {
                    canceled(dataSnapshot)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    completed(dataSnapshot)
                }
            }

            when(mode) {
                MODE_SINGLE -> reference?.addListenerForSingleValueEvent(eventListener)
                MODE_CONTINUOUS -> reference?.addValueEventListener(eventListener)
            }

            val finalRef = reference
            if(finalRef != null) {
                listenerMap.put(finalRef, eventListener)
            }
        }
    }
}