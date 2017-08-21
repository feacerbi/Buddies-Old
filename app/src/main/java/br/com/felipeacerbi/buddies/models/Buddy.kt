package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Buddy(
        var name: String = "",
        var animal: String = "",
        var breed: String = "",
        var photo: String = "",
        var tagId: String = "",
        var created: Long = System.currentTimeMillis(),
        var owners: Map<String, Boolean> = HashMap(),
        var followers: Map<String, Boolean> = HashMap(),
        var posts: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_ANIMAL_CHILD = "animal"
        val DATABASE_BREED_CHILD = "breed"
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_TAG_CHILD = "tagId"
        val DATABASE_CREATED_CHILD = "created"
        val DATABASE_OWNS_CHILD = "owns"
        val DATABASE_FOLLOWS_CHILD = "follows"
        val DATABASE_POSTS_CHILD = "posts"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    constructor(buddyInfo: BuddyInfo): this() {
        name = buddyInfo.name
        animal = buddyInfo.animal
        breed = buddyInfo.breed
        photo = buddyInfo.photo
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_ANIMAL_CHILD, animal),
            Pair(DATABASE_BREED_CHILD, breed),
            Pair(DATABASE_PHOTO_CHILD, photo),
            Pair(DATABASE_TAG_CHILD, tagId),
            Pair(DATABASE_CREATED_CHILD, created),
            Pair(DATABASE_OWNS_CHILD, owners),
            Pair(DATABASE_FOLLOWS_CHILD, followers),
            Pair(DATABASE_POSTS_CHILD, posts))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        animal = dataSnapshot.child(DATABASE_ANIMAL_CHILD).value as String
        breed = dataSnapshot.child(DATABASE_BREED_CHILD).value as String
        photo = dataSnapshot.child(DATABASE_PHOTO_CHILD).value as String
        tagId = dataSnapshot.child(DATABASE_TAG_CHILD).value as String
        created = dataSnapshot.child(DATABASE_CREATED_CHILD).value as Long

        val ownersSnapshot = dataSnapshot.child(DATABASE_OWNS_CHILD).value
        if(checkNull(ownersSnapshot)) owners = ownersSnapshot as Map<String, Boolean>

        val followersSnapshot = dataSnapshot.child(DATABASE_FOLLOWS_CHILD).value
        if(checkNull(followersSnapshot)) followers = followersSnapshot as Map<String, Boolean>

        val postsSnapshot = dataSnapshot.child(DATABASE_POSTS_CHILD).value
        if(checkNull(postsSnapshot)) posts = postsSnapshot as Map<String, Boolean>
    }

    fun toBuddyInfo(): BuddyInfo  = BuddyInfo(name, animal, breed, photo)

    fun checkNull(value: Any?) = value != null

}