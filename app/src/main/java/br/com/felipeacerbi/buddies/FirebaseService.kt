package br.com.felipeacerbi.buddies

import br.com.felipeacerbi.buddies.models.BaseTag
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.toUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
class FirebaseService : FirebaseInstanceIdService() {

    companion object {
        val DATABASE_USERS_PATH = "users/"
        val DATABASE_PETS_PATH = "pets/"
        val DATABASE_TAGS_PATH = "tags/"
        val DATABASE_IDTOKEN_CHILD = "idToken"
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_EMAIL_CHILD = "email"
        val DATABASE_BUDDIES_CHILD = "buddies"
        val DATABASE_OWNERS_CHILD = "owners"
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_PETNAME_CHILD = "petName"
    }

    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseInstanceID = FirebaseInstanceId.getInstance()
    val firebaseDB = FirebaseDatabase.getInstance()

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        getUserReference(getCurrentUsername()).child(DATABASE_IDTOKEN_CHILD).setValue(getAppIDToken())
    }

    // DB API
    fun getDBReference(path: String) = firebaseDB.getReference(path)
    fun updateDB(childUpdates: HashMap<String, Any>) {
        getDBReference("").updateChildren(childUpdates)
    }
    fun getAppIDToken() = firebaseInstanceID.token ?: ""
    fun signOut() = firebaseAuth.signOut()

    // Users API
    fun getUserReference(username: String) = getUsersReference().child(username)
    fun getUsersReference() = getDBReference(DATABASE_USERS_PATH)
    fun getCurrentUser() = firebaseAuth.currentUser
    fun getCurrentUserDisplayName() = getCurrentUser()?.displayName ?: ""
    fun getCurrentUserEmail() = getCurrentUser()?.email ?: ""
    fun getCurrentUsername() = getCurrentUserEmail().toUsername()
    fun registerUser() {
        val user = User(
                getCurrentUserDisplayName(),
                getCurrentUserEmail(),
                getAppIDToken())

        val childUpdates = HashMap<String, Any>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"

        childUpdates.put(currentUserPath, user.toMap())

        updateDB(childUpdates)
    }

    // Pets API
    fun getPetReference(petId: String) = getPetsReference().child(petId)
    fun getPetsReference() = getDBReference(DATABASE_PETS_PATH)
    fun getUserPetsReference(username: String) = getUserReference(username).child(DATABASE_BUDDIES_CHILD)
    fun addNewPet(baseTag: BaseTag, buddy: Buddy) {
        val petKey = getDBReference(DATABASE_PETS_PATH).push().key
        val tagKey = getDBReference(DATABASE_TAGS_PATH).push().key

        baseTag.petId = petKey
        buddy.tagId = baseTag.id

        (buddy.owners as HashMap).put(getCurrentUsername(), getCurrentUserDisplayName())

        val childUpdates = HashMap<String, Any>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
        val tagPath = DATABASE_TAGS_PATH + tagKey + "/"
        val petPath = DATABASE_PETS_PATH + petKey + "/"

        with(childUpdates) {
            put(currentUserPath + DATABASE_BUDDIES_CHILD + "/" + petKey, buddy.name)
            put(tagPath, baseTag.toMap())
            put(petPath, buddy.toMap())
        }

        updateDB(childUpdates)
    }
}