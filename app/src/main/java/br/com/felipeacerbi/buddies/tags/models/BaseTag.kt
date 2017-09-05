package br.com.felipeacerbi.buddies.tags.models

import com.google.firebase.database.DataSnapshot
import org.parceler.Parcel

@Parcel
data class BaseTag(
        var id: String = "",
        var petId: String = "",
        var verified: Boolean = false,
        var created: Long = System.currentTimeMillis()) {

    companion object {
        val DATABASE_ID_CHILD = "id"
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_VERIFIED_CHILD = "verified"
        val DATABASE_CREATED_CHILD = "created"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf<String, Any>(
            Pair(DATABASE_ID_CHILD, id),
            Pair(DATABASE_PETID_CHILD, petId),
            Pair(DATABASE_VERIFIED_CHILD, verified),
            Pair(DATABASE_CREATED_CHILD, created))

    fun fromMap(dataSnapshot: DataSnapshot) {
        id = dataSnapshot.child(DATABASE_ID_CHILD).value as String
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
        verified = dataSnapshot.child(DATABASE_VERIFIED_CHILD).value as Boolean
        created = dataSnapshot.child(DATABASE_CREATED_CHILD).value as Long
    }
}