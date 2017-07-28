package br.com.felipeacerbi.buddies.firebase

import android.util.Log
import br.com.felipeacerbi.buddies.models.Buddy
import br.com.felipeacerbi.buddies.models.Request
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.*
import javax.inject.Singleton
import kotlin.collections.ArrayList

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
        val DATABASE_PHOTO_CHILD = "photo"
        val DATABASE_BREED_CHILD = "breed"
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

        getUserReference(getCurrentUserUID()).child(DATABASE_IDTOKEN_CHILD).setValue(getAppIDToken())
    }

    // DB API
    fun getDatabaseReference(path: String) = firebaseDB.getReference(path)
    fun getAppIDToken() = firebaseInstanceID.token ?: ""

    fun updateDB(childUpdates: HashMap<String, Any?>) {
        getDatabaseReference("").updateChildren(childUpdates)
    }

    // Users API
    fun getUserReference(username: String) = getUsersReference().child(username)
    fun getUsersReference() = getDatabaseReference(DATABASE_USERS_PATH)
    fun getCurrentUser() = firebaseAuth.currentUser
    fun getCurrentUserDisplayName() = getCurrentUser()?.displayName ?: ""
    fun getCurrentUserEmail() = getCurrentUser()?.email ?: ""
    fun getCurrentUserUID() = getCurrentUser()?.uid ?: ""
    fun getCurrentUserPicture() = getCurrentUser()?.photoUrl
    fun getCurrentUserProviders(): List<UserInfo> = getCurrentUser()?.providerData ?: ArrayList<UserInfo>()

    fun registerUser(user: User) {
        user.idToken = getAppIDToken()

        val childUpdates = HashMap<String, Any?>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"

        childUpdates.put(currentUserPath, user.toMap())

        updateDB(childUpdates)
    }

    fun updateUser(user: User?) {
        if(user != null) {

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"

            childUpdates.put(currentUserPath + DATABASE_NAME_CHILD, user.name)
            childUpdates.put(currentUserPath + DATABASE_EMAIL_CHILD, user.email)
            childUpdates.put(currentUserPath + DATABASE_PHOTO_CHILD, user.photo)

            updateDB(childUpdates)
        }
    }

    fun checkUserObservable(username: String): Observable<Pair<Boolean, User>> {
        return Observable.create {
            subscriber ->
            getUserReference(username).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Adding user cancelled: " + error.toString())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    handleUserSnapshot(subscriber, dataSnapshot)
                }
            })
        }
    }

    private fun  handleUserSnapshot(subscriber: ObservableEmitter<Pair<Boolean, User>>, dataSnapshot: DataSnapshot?) {
        if(dataSnapshot != null && dataSnapshot.childrenCount > 0) {
            val foundUser = User(dataSnapshot)
            subscriber.onNext(Pair(true, foundUser))
            Log.d(TAG, "User found")
        } else {
            subscriber.onNext(Pair(false, User()))
            Log.d(TAG, "User not found")
        }
        subscriber.onComplete()
    }

    // TAGs API
    fun getTagsReference() = getDatabaseReference(DATABASE_TAGS_PATH)
    fun getTagReference(tagId: String) = getTagsReference().child(tagId)

    fun checkTagObservable(baseTag: BaseTag): Observable<BaseTag> {
        return Observable.create {
            subscriber ->
            getTagsReference().orderByChild(DATABASE_ID_CHILD).equalTo(baseTag.id).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Adding pet cancelled: " + error.toString())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
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

    // Pets API
    fun getPetReference(petId: String) = getPetsReference().child(petId)
    fun getPetsReference() = getDatabaseReference(DATABASE_PETS_PATH)
    fun getUserPetsReference(username: String) = getUserReference(username).child(DATABASE_OWNS_CHILD)
    fun getUserFollowReference(username: String) = getUserReference(username).child(DATABASE_FOLLOWS_CHILD)

    fun addNewPet(baseTag: BaseTag, buddy: Buddy) {
        if(baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding new pet")
            val petKey = getPetsReference().push().key
            val tagKey = getTagsReference().push().key

            baseTag.petId = petKey
            buddy.tagId = baseTag.id

            (buddy.owners as HashMap).put(getCurrentUserUID(), true)

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"
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
                    val requestKey = getDatabaseReference(DATABASE_REQUESTS_PATH).push().key
                    val request = Request(getCurrentUserUID(), baseTag.petId, Calendar.getInstance().timeInMillis.toString())
                    val childUpdates = HashMap<String, Any?>()

                    with(childUpdates) {
                        put(DATABASE_REQUESTS_PATH + requestKey, request.toMap())
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

                with(childUpdates) {
                    if(allow) {
                        put(userPath + DATABASE_OWNS_CHILD + "/" + request.petId, true)
                        put(petPath + DATABASE_OWNS_CHILD + "/" + request.username, true)
                    }
                    put(DATABASE_REQUESTS_PATH + key, null)
                    dataSnapshot?.children?.forEach { put(DATABASE_USERS_PATH + it.key + "/" + DATABASE_REQUESTS_CHILD + "/" + key, null) }
                }

                updateDB(childUpdates)
            }
        })
    }

    fun addFollowPet(baseTag: BaseTag) {
        if(!baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding follow pet")
            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"
            val currentPetPath = DATABASE_PETS_PATH + baseTag.petId + "/"

            with(childUpdates) {
                put(currentUserPath + DATABASE_FOLLOWS_CHILD + "/" + baseTag.petId, true)
                put(currentPetPath + DATABASE_FOLLOWS_CHILD + "/" + getCurrentUserUID(), true)
            }

            updateDB(childUpdates)
        } else {
            Log.d(TAG, "Error, follow pet not found")
        }
    }

    fun removePetFromUser(ref: String, petId: String) {
        if(!petId.isEmpty()) {
            Log.d(TAG, "Removing pet from owner")
            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/" + ref + "/" + petId
            val currentPetPath = DATABASE_PETS_PATH + petId + "/" + ref + "/" + getCurrentUserUID()

            with(childUpdates) {
                put(currentUserPath, null)
                put(currentPetPath, null)
            }

            updateDB(childUpdates)

            getPetReference(petId).child(DATABASE_OWNS_CHILD).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Checking pet cancelled: " + error.toString())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if(dataSnapshot != null && dataSnapshot.childrenCount > 0) {
                        Log.d(TAG, "Owner found")
                    } else {
                        Log.d(TAG, "Owner not found")
                        removePet(petId)
                    }
                }
            })
        } else {
            Log.d(TAG, "Error, follow pet not found")
        }
    }

    fun removePet(petId: String) {
        if(!petId.isEmpty()) {
            Log.d(TAG, "Removing pet")
            getPetReference(petId).removeValue()
            getTagsReference().orderByChild(DATABASE_PETID_CHILD).equalTo(petId).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Removing cancelled: " + error.toString())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if(dataSnapshot != null && dataSnapshot.childrenCount > 0) {
                        dataSnapshot.children.forEach { getTagReference(it.key).removeValue() }
                    }

                }
            })
        }
    }

    fun updatePet(buddy: Buddy?, petId: String) {
        if(buddy != null) {

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_PETS_PATH + petId + "/"

            childUpdates.put(currentUserPath + DATABASE_NAME_CHILD, buddy.name)
            childUpdates.put(currentUserPath + DATABASE_BREED_CHILD, buddy.breed)
            childUpdates.put(currentUserPath + DATABASE_PHOTO_CHILD, buddy.photo)

            updateDB(childUpdates)
        }
    }

    // Requests API
    fun getRequestsReference() = getDatabaseReference(DATABASE_REQUESTS_PATH)
    fun getRequestReference(requestId: String) = getRequestsReference().child(requestId)
    fun getUserRequestsReference(username: String) = getUserReference(username).child(DATABASE_REQUESTS_CHILD)

    fun queryBuddies() = getUserPetsReference(getCurrentUserUID())
    fun queryFollow() = getUserFollowReference(getCurrentUserUID())
    fun queryRequests() = getUserRequestsReference(getCurrentUserUID())
}