package br.com.felipeacerbi.buddies.models

import com.google.firebase.database.DataSnapshot

data class FriendlyItem(
        var name: String = "",
        var description: String = "",
        var icon: Int = 0) {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_DESCRIPTION_CHILD = "description"
        val DATABASE_ICON_CHILD = "icon"
    }

    constructor(dataSnapshot: DataSnapshot): this() {
        fromMap(dataSnapshot)
    }

    private fun fromMap(dataSnapshot: DataSnapshot) {
        name = dataSnapshot.child(DATABASE_NAME_CHILD).value as String
        description = dataSnapshot.child(DATABASE_DESCRIPTION_CHILD).value as String
        icon = dataSnapshot.child(DATABASE_ICON_CHILD).value as Int
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_DESCRIPTION_CHILD, description),
            Pair(DATABASE_ICON_CHILD, icon))

}