package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class User(
        var name: String = "",
        var email: String = "",
        var photo: String = "",
        var idToken: String = "",
        var latlong: LatLong = LatLong(0.0, 0.0),
        var created: Long = System.currentTimeMillis(),
        var buddies: Map<String, Boolean> = HashMap(),
        var following: Map<String, Boolean> = HashMap(),
        var requests: Map<String, Boolean> = HashMap(),
        var places: Map<String, Boolean> = HashMap(),
        var posts: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_EMAIL_CHILD = "email"
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_IDTOKEN_CHILD = "idToken"
        val DATABASE_LATLONG_CHILD = "latlong"
        val DATABASE_CREATED_CHILD = "created"
        val DATABASE_OWNS_CHILD = "owns"
        val DATABASE_FOLLOWS_CHILD = "follows"
        val DATABASE_REQUESTS_CHILD = "requests"
        val DATABASE_PLACES_CHILD = "places"
        val DATABASE_POSTS_CHILD = "posts"
    }

    constructor(dataSnapshot: DataSnapshot) : this() {
        fromMap(dataSnapshot)
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_EMAIL_CHILD, email),
            Pair(DATABASE_PHOTO_CHILD, photo),
            Pair(DATABASE_IDTOKEN_CHILD, idToken),
            Pair(DATABASE_LATLONG_CHILD, latlong),
            Pair(DATABASE_CREATED_CHILD, created),
            Pair(DATABASE_OWNS_CHILD, buddies),
            Pair(DATABASE_FOLLOWS_CHILD, following),
            Pair(DATABASE_REQUESTS_CHILD, requests),
            Pair(DATABASE_PLACES_CHILD, places),
            Pair(DATABASE_REQUESTS_CHILD, requests),
            Pair(DATABASE_POSTS_CHILD, posts))

    fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        email = dataSnapshot.child(DATABASE_EMAIL_CHILD).value as String
        photo = dataSnapshot.child(DATABASE_PHOTO_CHILD).value as String
        idToken = dataSnapshot.child(DATABASE_IDTOKEN_CHILD).value as String
        latlong = dataSnapshot.child(DATABASE_LATLONG_CHILD).getValue(LatLong::class.java) ?: LatLong(0.0, 0.0)
        created = dataSnapshot.child(DATABASE_CREATED_CHILD).value as Long

        val buddiesSnapshot = dataSnapshot.child(DATABASE_OWNS_CHILD).value
        if(checkNull(buddiesSnapshot)) buddies = buddiesSnapshot as Map<String, Boolean>

        val followingSnapshot = dataSnapshot.child(DATABASE_FOLLOWS_CHILD).value
        if(checkNull(followingSnapshot)) following = followingSnapshot as Map<String, Boolean>

        val placesSnapshot = dataSnapshot.child(DATABASE_PLACES_CHILD).value
        if(checkNull(placesSnapshot)) places = placesSnapshot as Map<String, Boolean>

        val requestsSnapshot = dataSnapshot.child(DATABASE_REQUESTS_CHILD).value
        if(checkNull(requestsSnapshot)) requests = requestsSnapshot as Map<String, Boolean>

        val postsSnapshot = dataSnapshot.child(DATABASE_POSTS_CHILD).value
        if(checkNull(postsSnapshot)) posts = postsSnapshot as Map<String, Boolean>
    }

    fun checkNull(value: Any?) = value != null
}