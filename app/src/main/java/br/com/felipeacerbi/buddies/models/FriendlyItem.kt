package br.com.felipeacerbi.buddies.models

import br.com.felipeacerbi.buddies.R
import com.google.firebase.database.DataSnapshot

data class FriendlyItem(
        var name: String = "",
        var description: String = "",
        var type: String = "") {

    companion object {
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_DESCRIPTION_CHILD = "description"
        val DATABASE_TYPE_CHILD = "type"

        val TYPE_FOOD = "food"
        val TYPE_WATER = "water"
        val TYPE_PLACE = "place"
    }

    constructor(dataSnapshot: DataSnapshot?): this() {
        fromMap(dataSnapshot)
    }

    private fun fromMap(dataSnapshot: DataSnapshot?) {
        name = dataSnapshot?.child(DATABASE_NAME_CHILD)?.value as String
        description = dataSnapshot.child(DATABASE_DESCRIPTION_CHILD).value as String
        type = dataSnapshot.child(DATABASE_TYPE_CHILD).value as String
    }

    fun toMap() = mapOf(
            Pair(DATABASE_NAME_CHILD, name),
            Pair(DATABASE_DESCRIPTION_CHILD, description),
            Pair(DATABASE_TYPE_CHILD, type))

    fun getIconFromType() =
            when(type) {
                TYPE_FOOD -> R.drawable.ic_local_dining_black_24dp
                TYPE_WATER -> R.drawable.ic_local_drink_black_24dp
                TYPE_PLACE -> R.drawable.ic_home_black_24dp
                else -> R.drawable.ic_pets_black_24dp
            }

}