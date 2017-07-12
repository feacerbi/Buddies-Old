package br.com.felipeacerbi.buddies.firebase

import android.util.Log
import br.com.felipeacerbi.buddies.nfc.tags.BaseTag
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.toUsername
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import io.reactivex.Observable
import java.util.*
import javax.inject.Singleton

/**
 * Created by felipe.acerbi on 06/07/2017.
 */
@Singleton
class FirebaseService : FirebaseInstanceIdService() {

    companion object {
        val TAG = "FirebaseService"
        val DATABASE_USERS_PATH = "users/"
        val DATABASE_PETS_PATH = "pets/"
        val DATABASE_TAGS_PATH = "tags/"
        val DATABASE_IDTOKEN_CHILD = "idToken"
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_EMAIL_CHILD = "email"
        val DATABASE_BUDDIES_CHILD = "buddies"
        val DATABASE_FOLLOWING_CHILD = "following"
        val DATABASE_FOLLOWERS_CHILD = "followers"
        val DATABASE_OWNERS_CHILD = "owners"
        val DATABASE_ID_CHILD = "id"
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

        childUpdates.put(currentUserPath + DATABASE_NAME_CHILD, user.name)
        childUpdates.put(currentUserPath + DATABASE_EMAIL_CHILD, user.email)
        childUpdates.put(currentUserPath + DATABASE_IDTOKEN_CHILD, user.idToken)

        updateDB(childUpdates)
    }

    // Pets API
    fun getPetReference(petId: String) = getPetsReference().child(petId)
    fun getPetsReference() = getDBReference(DATABASE_PETS_PATH)
    fun getUserPetsReference(username: String) = getUserReference(username).child(DATABASE_BUDDIES_CHILD)
    fun getUserFollowReference(username: String) = getUserReference(username).child(DATABASE_FOLLOWING_CHILD)
    fun addNewPet(baseTag: BaseTag, buddy: Buddy) {
        if(baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding new pet")
            val petKey = getDBReference(DATABASE_PETS_PATH).push().key
            val tagKey = getDBReference(DATABASE_TAGS_PATH).push().key

            baseTag.petId = petKey
            buddy.tagId = baseTag.id

            (buddy.owners as HashMap).put(getCurrentUsername(), true)

            val childUpdates = HashMap<String, Any>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
            val tagPath = DATABASE_TAGS_PATH + tagKey + "/"
            val petPath = DATABASE_PETS_PATH + petKey + "/"

            with(childUpdates) {
                put(currentUserPath + DATABASE_BUDDIES_CHILD + "/" + petKey, true)
                put(tagPath, baseTag.toMap())
                put(petPath, buddy.toMap())
            }

            updateDB(childUpdates)
        } else {
            Log.d(TAG, "Error, new pet found")
        }
    }
    fun addPetOwner(baseTag: BaseTag) {
        if(!baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding owner pet")
            val childUpdates = HashMap<String, Any>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
            val currentPetPath = DATABASE_PETS_PATH + baseTag.petId + "/"

            with(childUpdates) {
                put(currentUserPath + DATABASE_BUDDIES_CHILD + "/" + baseTag.petId, true)
                put(currentPetPath + DATABASE_OWNERS_CHILD + "/" + getCurrentUsername(), true)
            }

            updateDB(childUpdates)
        } else {
            Log.d(TAG, "Error, owner pet not found")
        }
    }
    fun addFollowPet(baseTag: BaseTag) {
        if(!baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding follow pet")
            val childUpdates = HashMap<String, Any>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
            val currentPetPath = DATABASE_PETS_PATH + baseTag.petId + "/"

            with(childUpdates) {
                put(currentUserPath + DATABASE_FOLLOWING_CHILD + "/" + baseTag.petId, true)
                put(currentPetPath + DATABASE_FOLLOWERS_CHILD + "/" + getCurrentUsername(), true)
            }

            updateDB(childUpdates)
        } else {
            Log.d(TAG, "Error, follow pet not found")
        }
    }
    fun checkPetObservable(baseTag: BaseTag): Observable<BaseTag> {
        return Observable.create {
            subscriber ->
            getDBReference(DATABASE_TAGS_PATH).orderByChild(DATABASE_ID_CHILD).equalTo(baseTag.id).addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Adding pet cancelled: " + error.toString())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    Log.d(TAG, "OnDataChange")
                    var foundTag = baseTag
                    if(dataSnapshot != null && dataSnapshot.childrenCount > 0) {
                        foundTag = BaseTag(dataSnapshot.children.first())
                        Log.d(TAG, "Tag found")
                    } else {
                        Log.d(TAG, "Tag not found")
                    }
                    subscriber.onNext(foundTag)
                    subscriber.onComplete()
                }
            })
        }
    }

}