package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
data class Request(
        var username: String = "",
        var petId: String = "") : Serializable {

    companion object {
        val DATABASE_USERNAME_CHILD = "username"
        val DATABASE_PETID_CHILD = "petId"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_USERNAME_CHILD, username),
            Pair(DATABASE_PETID_CHILD, petId))

    fun fromMap(dataSnapshot: DataSnapshot) {
        username = dataSnapshot.child(DATABASE_USERNAME_CHILD).value as String
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
    }

}