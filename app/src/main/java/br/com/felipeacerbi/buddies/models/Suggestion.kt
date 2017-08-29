package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

class Suggestion(
        var userId: String = "",
        var place: Place? = null,
        var status: String = "Requested",
        var timestamp: Long = System.currentTimeMillis()) {

    companion object {
        val DATABASE_USERID_CHILD = "userId"
        val DATABASE_PLACE_CHILD = "place"
        val DATABASE_STATUS_CHILD = "status"
        val DATABASE_TIMESTAMP_CHILD = "timestamp"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    private fun fromMap(dataSnapshot: DataSnapshot) {
        userId = dataSnapshot.child(DATABASE_USERID_CHILD).value as String
        place = dataSnapshot.child(DATABASE_PLACE_CHILD).value as Place
        status = dataSnapshot.child(DATABASE_STATUS_CHILD).value as String
        timestamp = dataSnapshot.child(DATABASE_TIMESTAMP_CHILD).value as Long
    }

    fun toMap() = mapOf(
            Pair(DATABASE_USERID_CHILD, userId),
            Pair(DATABASE_PLACE_CHILD, place?.toMap()),
            Pair(DATABASE_STATUS_CHILD, status),
            Pair(DATABASE_TIMESTAMP_CHILD, timestamp))
}