package br.com.felipeacerbi.buddies

import android.content.Context
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType
import br.com.felipeacerbi.buddies.models.NFCTag
import br.com.felipeacerbi.buddies.utils.toUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import javax.inject.Inject

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
class FirebaseService @Inject constructor(private val context: Context? = null) : FirebaseInstanceIdService() {

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

        val user = getCurrentUserEmail()
        getUserReference(user.toUsername()).child(DATABASE_IDTOKEN_CHILD).setValue(getAppIDToken())
    }

    fun getUserReference(username: String) = getDBReference(DATABASE_USERS_PATH).child(username)

    fun getDBReference(path: String) = firebaseDB.getReference(path)

    fun getCurrentUser() = firebaseAuth.currentUser

    fun getCurrentUserDisplayName() = getCurrentUser()?.displayName ?: ""

    fun getCurrentUserEmail() = getCurrentUser()?.email ?: ""

    fun getCurrentUsername() = getCurrentUserEmail().toUsername()

    fun registerUser() {

        val childUpdates = HashMap<String, Any>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"

        with(childUpdates) {
            put(currentUserPath + DATABASE_IDTOKEN_CHILD, getAppIDToken())
            put(currentUserPath + DATABASE_NAME_CHILD, getCurrentUserDisplayName())
            put(currentUserPath + DATABASE_EMAIL_CHILD, getCurrentUserEmail())
        }

        updateDB(childUpdates)
    }

    fun addNewPet(nfcTag: NFCTag) {
        nfcTag.petId = getDBReference(DATABASE_PETS_PATH).push().key

        val childUpdates = HashMap<String, Any>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
        val tagPath = DATABASE_TAGS_PATH + nfcTag.id + "/"

        with(childUpdates) {
            put(currentUserPath + DATABASE_BUDDIES_CHILD + "/" + nfcTag.petId, nfcTag.petName)
            put(tagPath + DATABASE_PETID_CHILD, nfcTag.petId)
            put(tagPath + DATABASE_PETNAME_CHILD, nfcTag.petName)
            //put(DATABASE_PETS_PATH + nfcTag.petId + "/" + DATABASE_OWNERS_CHILD, getCurrentUserEmail().toUsername())
        }

        updateDB(childUpdates)
    }

    fun updateDB(childUpdates: HashMap<String, Any>) {
        getDBReference("").updateChildren(childUpdates)
    }

    fun getPetReference(petId: String) = getPetsReference().child(petId)

    fun getPetsReference() = getDBReference(DATABASE_PETS_PATH)

    fun getUserPetsReference(username: String) = getUserReference(username).child(DATABASE_BUDDIES_CHILD)

    fun getAppIDToken() = firebaseInstanceID.token ?: ""

    fun signOut() = firebaseAuth.signOut()
}