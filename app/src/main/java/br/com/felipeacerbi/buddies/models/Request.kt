package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
data class Request(
        var username: String = "",
        var petId: String = "",
        var status: String = "") : Serializable {

    companion object {
        val DATABASE_USERNAME_CHILD = "username"
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_STATUS_CHILD = "status"

        val STATUS_OPEN = "open"
        val STATUS_ACCEPTED = "accepted"
        val STATUS_REFUSED = "refused"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_USERNAME_CHILD, username),
            Pair(DATABASE_PETID_CHILD, petId),
            Pair(DATABASE_STATUS_CHILD, status))

    fun fromMap(dataSnapshot: DataSnapshot) {
        username = dataSnapshot.child(DATABASE_USERNAME_CHILD).value as String
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
        status = dataSnapshot.child(DATABASE_STATUS_CHILD).value as String
    }
}