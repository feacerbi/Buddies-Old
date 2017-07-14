package br.com.felipeacerbi.buddies.firebase

import android.util.Log
import br.com.felipeacerbi.buddies.nfc.tags.BaseTag
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.Request
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.utils.toUsername
import com.firebase.ui.database.ObservableSnapshotArray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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
        val DATABASE_REQUESTS_PATH = "requests/"
        val DATABASE_IDTOKEN_CHILD = "idToken"
        val DATABASE_NAME_CHILD = "name"
        val DATABASE_EMAIL_CHILD = "email"
        val DATABASE_FOLLOWS_CHILD = "follows"
        val DATABASE_OWNS_CHILD = "owns"
        val DATABASE_ID_CHILD = "id"
        val DATABASE_PETID_CHILD = "petId"
        val DATABASE_REQUESTS_CHILD = "requests"
        val DATABASE_STATUS_CHILD = "status"
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
    fun updateDB(childUpdates: HashMap<String, Any?>) {
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

        val childUpdates = HashMap<String, Any?>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"

        childUpdates.put(currentUserPath + DATABASE_NAME_CHILD, user.name)
        childUpdates.put(currentUserPath + DATABASE_EMAIL_CHILD, user.email)
        childUpdates.put(currentUserPath + DATABASE_IDTOKEN_CHILD, user.idToken)

        updateDB(childUpdates)
    }

    // Pets API
    fun getPetReference(petId: String) = getPetsReference().child(petId)
    fun getPetsReference() = getDBReference(DATABASE_PETS_PATH)
    fun getUserPetsReference(username: String) = getUserReference(username).child(DATABASE_OWNS_CHILD)
    fun getUserFollowReference(username: String) = getUserReference(username).child(DATABASE_FOLLOWS_CHILD)
    fun addNewPet(baseTag: BaseTag, buddy: Buddy) {
        if(baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding new pet")
            val petKey = getDBReference(DATABASE_PETS_PATH).push().key
            val tagKey = getDBReference(DATABASE_TAGS_PATH).push().key

            baseTag.petId = petKey
            buddy.tagId = baseTag.id

            (buddy.owners as HashMap).put(getCurrentUsername(), true)

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
            val tagPath = DATABASE_TAGS_PATH + tagKey + "/"
            val petPath = DATABASE_PETS_PATH + petKey + "/"

            with(childUpdates) {
                put(currentUserPath + DATABASE_OWNS_CHILD + "/" + petKey, true)
                put(tagPath, baseTag.toMap())
                put(petPath, buddy.toMap())
            }

            updateDB(childUpdates)
        } else {
            Log.d(TAG, "Error, new pet found")
        }
    }
    fun addPetOwnerRequest(baseTag: BaseTag) {
        if(!baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding owner pet")

            getPetReference(baseTag.petId).child(DATABASE_OWNS_CHILD).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Error, owner pet cancelled " + error?.message)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    val requestKey = getDBReference(DATABASE_REQUESTS_PATH).push().key
                    val request = Request(getCurrentUsername(), baseTag.petId, Request.STATUS_OPEN)
                    val childUpdates = HashMap<String, Any?>()

                    with(childUpdates) {
                        put(DATABASE_REQUESTS_PATH + "/" + requestKey, request.toMap())
                        dataSnapshot?.children?.forEach { put(DATABASE_USERS_PATH + it.key + "/" + DATABASE_REQUESTS_CHILD + "/" + requestKey, true) }
                    }

                    updateDB(childUpdates)
                }
            })

        } else {
            Log.d(TAG, "Error, owner pet not found")
        }
    }
    fun allowPetOwner(request: Request, key: String, allow: Boolean) {
        Log.d(TAG, "Allowing owner pet")

        getPetReference(request.petId).child(DATABASE_OWNS_CHILD).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Log.d(TAG, "Error, owner pet allow cancelled " + error?.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val childUpdates = HashMap<String, Any?>()
                val userPath = DATABASE_USERS_PATH + request.username + "/"
                val petPath = DATABASE_PETS_PATH + request.petId + "/"
                val status = if(allow) Request.STATUS_ACCEPTED else Request.STATUS_REFUSED

                with(childUpdates) {
                    if(allow) {
                        put(userPath + DATABASE_OWNS_CHILD + "/" + request.petId, true)
                        put(petPath + DATABASE_OWNS_CHILD + "/" + request.username, true)
                    }
                    put(DATABASE_REQUESTS_PATH + key + "/" + DATABASE_STATUS_CHILD, status)
                }

                updateDB(childUpdates)
            }
        })
    }
    fun addFollowPet(baseTag: BaseTag) {
        if(!baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding follow pet")
            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/"
            val currentPetPath = DATABASE_PETS_PATH + baseTag.petId + "/"

            with(childUpdates) {
                put(currentUserPath + DATABASE_FOLLOWS_CHILD + "/" + baseTag.petId, true)
                put(currentPetPath + DATABASE_FOLLOWS_CHILD + "/" + getCurrentUsername(), true)
            }

            updateDB(childUpdates)
        } else {
            Log.d(TAG, "Error, follow pet not found")
        }
    }
    fun checkPetObservable(baseTag: BaseTag): Observable<BaseTag> {
        return Observable.create {
            subscriber ->
            getDBReference(DATABASE_TAGS_PATH).orderByChild(DATABASE_ID_CHILD).equalTo(baseTag.id).addListenerForSingleValueEvent(object: ValueEventListener {
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
    fun removePet(ref: String, petId: String) {
        if(!petId.isEmpty()) {
            Log.d(TAG, "Removing pet")
            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUsername() + "/" + ref + "/" + petId
            val currentPetPath = DATABASE_PETS_PATH + petId + "/" + ref + "/" + getCurrentUsername()

            with(childUpdates) {
                put(currentUserPath, null)
                put(currentPetPath, null)
            }

            updateDB(childUpdates)

            // TODO Remove pet if no owners
        } else {
            Log.d(TAG, "Error, follow pet not found")
        }
    }

    fun getRequestsReference() = getDBReference(DATABASE_REQUESTS_PATH).orderByChild(DATABASE_PETID_CHILD).ref
    fun getRequestReference(requestId: String) = getRequestsReference().child(requestId)
    fun getUserRequestsReference(username: String) = getUserReference(username).child(DATABASE_REQUESTS_CHILD)

    fun queryBuddies() = getUserPetsReference(getCurrentUsername())
    fun queryFollow() = getUserFollowReference(getCurrentUsername())
    fun queryRequests() = getUserRequestsReference(getCurrentUsername()).orderByChild(DATABASE_STATUS_CHILD).equalTo(Request.STATUS_OPEN).ref
}