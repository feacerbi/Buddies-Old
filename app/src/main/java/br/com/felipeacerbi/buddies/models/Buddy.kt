package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
data class Buddy(
        var name: String = "",
        var breed: String = "",
        var tagId: String = "",
        var owners: Map<String, String> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_BREED_CHILD = "breed"
        val DATABASE_TAG_CHILD = "tagId"
        val DATABASE_OWNERS_CHILD = "owners"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf<String, Any>(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_BREED_CHILD, breed),
            Pair(DATABASE_TAG_CHILD, tagId),
            Pair(DATABASE_OWNERS_CHILD, owners))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        breed = dataSnapshot.child(DATABASE_BREED_CHILD).value as String
        tagId = dataSnapshot.child(DATABASE_TAG_CHILD).value as String
        owners = dataSnapshot.child(DATABASE_OWNERS_CHILD).value as Map<String, String>
    }

}