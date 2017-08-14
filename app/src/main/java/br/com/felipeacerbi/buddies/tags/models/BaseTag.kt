package br.com.felipeacerbi.buddies.tags.models

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

open class BaseTag(
        var id: String = "",
        var petId: String = "",
        var created: Long = System.currentTimeMillis()) : Serializable {

    companion object {
        val DATABASE_ID_CHILD = "id"
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_CREATED_CHILD = "created"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf<String, Any>(
            Pair(DATABASE_ID_CHILD, id),
            Pair(DATABASE_PETID_CHILD, petId),
            Pair(DATABASE_CREATED_CHILD, created))

    fun fromMap(dataSnapshot: DataSnapshot) {
        id = dataSnapshot.child(DATABASE_ID_CHILD).value as String
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
        created = dataSnapshot.child(DATABASE_CREATED_CHILD).value as Long
    }
}