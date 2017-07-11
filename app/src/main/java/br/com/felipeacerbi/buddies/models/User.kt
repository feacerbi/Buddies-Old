package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

/**
 * Created by felipe.acerbi on 07/07/2017.
 */
data class User(
        var name: String = "",
        var email: String = "",
        var idToken: String = "",
        var buddies: Map<String, Boolean> = HashMap(),
        var following: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_EMAIL_CHILD = "email"
        val DATABASE_IDTOKEN_CHILD = "idToken"
        val DATABASE_BUDDIES_CHILD = "buddies"
        val DATABASE_FOLLOWING_CHILD = "following"
    }

    constructor(dataSnapshot: DataSnapshot) : this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_EMAIL_CHILD, email),
            Pair(DATABASE_IDTOKEN_CHILD, idToken),
            Pair(DATABASE_BUDDIES_CHILD, buddies),
            Pair(DATABASE_FOLLOWING_CHILD, following))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        email = dataSnapshot.child(DATABASE_EMAIL_CHILD).value as String
        idToken = dataSnapshot.child(DATABASE_IDTOKEN_CHILD).value as String

        val buddiesSnapshot = dataSnapshot.child(DATABASE_BUDDIES_CHILD).value
        if(checkNull(buddiesSnapshot)) buddies = buddiesSnapshot as Map<String, Boolean>

        val followingSnapshot = dataSnapshot.child(DATABASE_FOLLOWING_CHILD).value
        if(checkNull(followingSnapshot)) following = followingSnapshot as Map<String, Boolean>
    }

    fun checkNull(value: Any?) = value != null
}