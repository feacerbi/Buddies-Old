package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class Request(
        var username: String = "",
        var petId: String = "",
        var timestamp: Long = System.currentTimeMillis()) {

    companion object {
        val DATABASE_USERNAME_CHILD = "username"
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_TIMESTAMP_CHILD = "timestamp"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_USERNAME_CHILD, username),
            Pair(DATABASE_PETID_CHILD, petId),
            Pair(DATABASE_TIMESTAMP_CHILD, timestamp))

    fun fromMap(dataSnapshot: DataSnapshot) {
        username = dataSnapshot.child(DATABASE_USERNAME_CHILD).value as String
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
        timestamp = dataSnapshot.child(DATABASE_TIMESTAMP_CHILD).value as Long
    }
}