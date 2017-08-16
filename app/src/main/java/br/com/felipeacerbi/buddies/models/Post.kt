package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class Post(
        var petId: String = "",
        var message: String = "",
        var photo: String = "",
        var location: String = "",
        var created: Long = System.currentTimeMillis(),
        var likes: Map<String, Boolean> = HashMap(),
        var comments: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_MESSAGE_CHILD = "message"
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_LOCATION_CHILD = "location"
        val DATABASE_CREATED_CHILD = "created"
        val DATABASE_LIKES_CHILD = "likes"
        val DATABASE_COMMENTS_CHILD = "comments"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_PETID_CHILD, petId),
            Pair(DATABASE_MESSAGE_CHILD, message),
            Pair(DATABASE_PHOTO_CHILD, photo),
            Pair(DATABASE_LOCATION_CHILD, location),
            Pair(DATABASE_CREATED_CHILD, created),
            Pair(DATABASE_LIKES_CHILD, likes),
            Pair(DATABASE_COMMENTS_CHILD, comments))

    fun fromMap(dataSnapshot: DataSnapshot) {
        petId = dataSnapshot.child(DATABASE_PETID_CHILD).value as String
        message = dataSnapshot.child(DATABASE_MESSAGE_CHILD).value as String
        photo = dataSnapshot.child(DATABASE_PHOTO_CHILD).value as String
        location = dataSnapshot.child(DATABASE_LOCATION_CHILD).value as String
        created = dataSnapshot.child(DATABASE_CREATED_CHILD).value as Long

        val likesSnapshot = dataSnapshot.child(DATABASE_LIKES_CHILD).value
        if(checkNull(likesSnapshot)) likes = likesSnapshot as Map<String, Boolean>

//        val commentsSnapshot = dataSnapshot.child(DATABASE_COMMENTS_CHILD).value
//        if(checkNull(commentsSnapshot)) comments = commentsSnapshot as Map<String, Boolean>
    }

    fun checkNull(value: Any?) = value != null

}