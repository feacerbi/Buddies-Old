package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

/**
 * Created by felipe.acerbi on 05/07/2017.
 */
data class BaseTag(
        var id: String = "",
        var petId: String = "") {

    companion object {
        val DATABASE_ID_CHILD = "id"
        val DATABASE_PETID_CHILD = "petId"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf<String, Any>(
            Pair(DATABASE_ID_CHILD, id),
            Pair(DATABASE_PETID_CHILD, petId))

    fun fromMap(dataSnapshot: DataSnapshot) {
        id = dataSnapshot.child(DATABASE_ID_CHILD).value as String
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
    }
}