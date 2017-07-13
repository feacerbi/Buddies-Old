package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
data class Buddy(
        var name: String = "",
        var breed: String = "",
        var tagId: String = "",
        var owners: Map<String, Boolean> = HashMap(),
        var followers: Map<String, Boolean> = HashMap()) : Serializable {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_BREED_CHILD = "breed"
        val DATABASE_TAG_CHILD = "tagId"
        val DATABASE_OWNERS_CHILD = "owns"
        val DATABASE_FOLLOWERS_CHILD = "follows"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_BREED_CHILD, breed),
            Pair(DATABASE_TAG_CHILD, tagId),
            Pair(DATABASE_OWNERS_CHILD, owners),
            Pair(DATABASE_FOLLOWERS_CHILD, followers))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        breed = dataSnapshot.child(DATABASE_BREED_CHILD).value as String
        tagId = dataSnapshot.child(DATABASE_TAG_CHILD).value as String

        val ownersSnapshot = dataSnapshot.child(DATABASE_OWNERS_CHILD).value
        if(checkNull(ownersSnapshot)) owners = ownersSnapshot as Map<String, Boolean>

        val followersSnapshot = dataSnapshot.child(DATABASE_FOLLOWERS_CHILD).value
        if(checkNull(followersSnapshot)) followers = followersSnapshot as Map<String, Boolean>
    }

    fun checkNull(value: Any?) = value != null

}