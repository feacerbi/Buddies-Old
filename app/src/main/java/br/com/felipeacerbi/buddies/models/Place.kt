package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class Place(
        var name: String = "",
        var address: String = "",
        var photo: String = "",
        var description: String = "",
        var category: String = "",
        var rating: Int = 0,
        var items: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_ADDRESS_CHILD = "address"
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_DESCRIPTION_CHILD = "description"
        val DATABASE_CATEGORY_CHILD = "category"
        val DATABASE_RATING_CHILD = "rating"
        val DATABASE_ITEMS_CHILD = "items"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    private fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD) as String
        address = dataSnapshot.child(DATABASE_ADDRESS_CHILD) as String
        photo = dataSnapshot.child(DATABASE_PHOTO_CHILD) as String
        description = dataSnapshot.child(DATABASE_DESCRIPTION_CHILD) as String
        rating = dataSnapshot.child(DATABASE_RATING_CHILD) as Int
        category = dataSnapshot.child(DATABASE_CATEGORY_CHILD) as String

        items = dataSnapshot.child(DATABASE_ITEMS_CHILD) as Map<String, Boolean>
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_ADDRESS_CHILD, address),
            Pair(DATABASE_PHOTO_CHILD, photo),
            Pair(DATABASE_DESCRIPTION_CHILD, description),
            Pair(DATABASE_CATEGORY_CHILD, category),
            Pair(DATABASE_RATING_CHILD, rating),
            Pair(DATABASE_ITEMS_CHILD, items))
}