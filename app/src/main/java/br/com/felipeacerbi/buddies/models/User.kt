package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

/**
 * Created by felipe.acerbi on 07/07/2017.
 */
data class User(
        var name: String = "",
        var email: String = "",
        var idToken: String = "",
        var buddies: Map<String, String> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_EMAIL_CHILD = "email"
        val DATABASE_IDTOKEN_CHILD = "idToken"
        val DATABASE_BUDDIES_CHILD = "buddies"
    }

    constructor(dataSnapshot: DataSnapshot) : this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_EMAIL_CHILD, email),
            Pair(DATABASE_IDTOKEN_CHILD, idToken),
            Pair(DATABASE_BUDDIES_CHILD, buddies))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        email = dataSnapshot.child(DATABASE_EMAIL_CHILD).value as String
        idToken = dataSnapshot.child(DATABASE_IDTOKEN_CHILD).value as String
        buddies = dataSnapshot.child(DATABASE_BUDDIES_CHILD).value as Map<String, String>
    }
}