package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot
import java.io.Serializable

data class Buddy(
        var name: String = "",
        var breed: String = "",
        var photo: String = "",
        var tagId: String = "",
        var owners: Map<String, Boolean> = HashMap(),
        var followers: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_BREED_CHILD = "breed"
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_TAG_CHILD = "tagId"
        val DATABASE_OWNS_CHILD = "owns"
        val DATABASE_FOLLOWS_CHILD = "follows"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    constructor(buddyInfo: BuddyInfo): this() {
        name = buddyInfo.name
        breed = buddyInfo.breed
        photo = buddyInfo.photo
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_BREED_CHILD, breed),
            Pair(DATABASE_PHOTO_CHILD, photo),
            Pair(DATABASE_TAG_CHILD, tagId),
            Pair(DATABASE_OWNS_CHILD, owners),
            Pair(DATABASE_FOLLOWS_CHILD, followers))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        breed = dataSnapshot.child(DATABASE_BREED_CHILD).value as String
        photo = dataSnapshot.child(DATABASE_PHOTO_CHILD).value as String
        tagId = dataSnapshot.child(DATABASE_TAG_CHILD).value as String

        val ownersSnapshot = dataSnapshot.child(DATABASE_OWNS_CHILD).value
        if(checkNull(ownersSnapshot)) owners = ownersSnapshot as Map<String, Boolean>

        val followersSnapshot = dataSnapshot.child(DATABASE_FOLLOWS_CHILD).value
        if(checkNull(followersSnapshot)) followers = followersSnapshot as Map<String, Boolean>
    }

    fun checkNull(value: Any?) = value != null

}