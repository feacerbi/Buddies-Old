package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class Place(
        var name: String = "",
        var address: String = "",
        var photo: String = "",
        var phone: String = "",
        var website: String = "",
        var description: String = "",
        var category: String = "",
        var items: Map<String, Boolean> = HashMap()) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_ADDRESS_CHILD = "address"
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_PHONE_CHILD = "phone"
        val DATABASE_WEBSITE_CHILD = "website"
        val DATABASE_DESCRIPTION_CHILD = "description"
        val DATABASE_CATEGORY_CHILD = "category"
        val DATABASE_ITEMS_CHILD = "items"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    private fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        address = dataSnapshot.child(DATABASE_ADDRESS_CHILD).value as String
        photo = dataSnapshot.child(DATABASE_PHOTO_CHILD).value as String
        phone = dataSnapshot.child(DATABASE_PHONE_CHILD).value as String
        website = dataSnapshot.child(DATABASE_WEBSITE_CHILD).value as String
        description = dataSnapshot.child(DATABASE_DESCRIPTION_CHILD).value as String
        category = dataSnapshot.child(DATABASE_CATEGORY_CHILD).value as String

        items = dataSnapshot.child(DATABASE_ITEMS_CHILD).value as Map<String, Boolean>
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_ADDRESS_CHILD, address),
            Pair(DATABASE_PHOTO_CHILD, photo),
            Pair(DATABASE_PHONE_CHILD, phone),
            Pair(DATABASE_WEBSITE_CHILD, website),
            Pair(DATABASE_DESCRIPTION_CHILD, description),
            Pair(DATABASE_CATEGORY_CHILD, category),
            Pair(DATABASE_ITEMS_CHILD, items))

    fun calcRating() = (items.size.toFloat() / 5 * 100).toInt()

    fun getRatingGrade() = (items.size.toFloat() / 5 * 10).toInt().toString()
}