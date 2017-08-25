package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class Comment(
        var posterId: String = "",
        var postId: String = "",
        var message: String = "",
        var timestamp: Long = System.currentTimeMillis(),
        var likes: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_POSTERID_CHILD = "posterId"
        val DATABASE_POSTID_CHILD = "postId"
        val DATABASE_MESSAGE_CHILD = "message"
        val DATABASE_TIMESTAMP_CHILD = "timestamp"
        val DATABASE_LIKES_CHILD = "likes"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_POSTERID_CHILD, posterId),
            Pair(DATABASE_POSTID_CHILD, postId),
            Pair(DATABASE_MESSAGE_CHILD, message),
            Pair(DATABASE_TIMESTAMP_CHILD, timestamp),
            Pair(DATABASE_LIKES_CHILD, likes))

    fun fromMap(dataSnapshot: DataSnapshot) {
        posterId = dataSnapshot.child(DATABASE_POSTERID_CHILD).value as String
        postId = dataSnapshot.child(DATABASE_POSTID_CHILD).value as String
        message = dataSnapshot.child(DATABASE_MESSAGE_CHILD).value as String
        timestamp = dataSnapshot.child(DATABASE_TIMESTAMP_CHILD).value as Long

        val likesSnapshot = dataSnapshot.child(DATABASE_LIKES_CHILD).value
        if(checkNull(likesSnapshot)) likes = likesSnapshot as Map<String, Boolean>
    }

    fun checkNull(value: Any?) = value != null

}