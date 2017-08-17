package br.com.felipeacerbi.buddies.firebase

import android.location.Location
import android.net.Uri
import android.util.Log
import br.com.felipeacerbi.buddies.models.*
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
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

@Singleton
class FirebaseService : FirebaseInstanceIdService() {

    companion object {
        val TAG = "FirebaseService"
        val DATABASE_USERS_PATH = "users/"
        val DATABASE_PETS_PATH = "pets/"
        val DATABASE_TAGS_PATH = "tags/"
        val DATABASE_REQUESTS_PATH = "requests/"
        val DATABASE_PLACES_PATH = "places/"
        val DATABASE_FITEMS_PATH = "fitems/"
        val DATABASE_ANIMALS_PATH = "animals/"
        val DATABASE_POSTS_PATH = "posts/"
    }

    val firebaseAuth = FirebaseAuth.getInstance()
    val firebaseInstanceID = FirebaseInstanceId.getInstance()
    val firebaseDB = FirebaseDatabase.getInstance()
    val firebaseStoreService = FirebaseStorageService()

    override fun onTokenRefresh() {
        super.onTokenRefresh()

        getUserReference(getCurrentUserUID()).child(User.DATABASE_IDTOKEN_CHILD).setValue(getAppIDToken())
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
//    fun getCurrentUserProviders(): List<UserInfo> = getCurrentUser()?.providerData ?: ArrayList<UserInfo>()

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

            childUpdates.put(currentUserPath + User.DATABASE_NAME_CHILD, user.name)
            childUpdates.put(currentUserPath + User.DATABASE_EMAIL_CHILD, user.email)
            childUpdates.put(currentUserPath + User.DATABASE_PHOTO_CHILD, user.photo)

            updateDB(childUpdates)
        }
    }

    fun registerUserLocation(location: Location) {
        val childUpdates = HashMap<String, Any?>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"

        childUpdates.put(currentUserPath + User.DATABASE_LATLONG_CHILD, LatLng(location.latitude, location.longitude))

        updateDB(childUpdates)
    }

    fun checkUserObservable(username: String): Observable<Pair<Boolean, User>> = Observable.create {
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

    fun checkTagObservable(baseTag: BaseTag): Observable<BaseTag> = Observable.create {
        subscriber ->
        getTagsReference().orderByChild(BaseTag.DATABASE_ID_CHILD).equalTo(baseTag.id).addListenerForSingleValueEvent(object: ValueEventListener {
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

    // Pets API
    fun getPetReference(petId: String) = getPetsReference().child(petId)
    fun getPetsReference() = getDatabaseReference(DATABASE_PETS_PATH)
    fun getUserPetsReference(username: String) = getUserReference(username).child(User.DATABASE_OWNS_CHILD)
    fun getUserFollowReference(username: String) = getUserReference(username).child(User.DATABASE_FOLLOWS_CHILD)

    fun addNewPet(baseTag: BaseTag, buddyInfo: BuddyInfo) {
        if(baseTag.petId.isEmpty()) {
            Log.d(TAG, "Adding new pet")
            val petKey = getPetsReference().push().key
            val tagKey = getTagsReference().push().key

            val buddy = Buddy(buddyInfo)

            baseTag.petId = petKey
            buddy.tagId = baseTag.id

            (buddy.owners as HashMap).put(getCurrentUserUID(), true)

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"
            val tagPath = DATABASE_TAGS_PATH + tagKey + "/"
            val petPath = DATABASE_PETS_PATH + petKey + "/"

            with(childUpdates) {
                put(currentUserPath + User.DATABASE_OWNS_CHILD + "/" + petKey, true)
                put(tagPath, baseTag.toMap())
            }

            if(buddyInfo.photo.isNotEmpty()) {
                uploadPetFile(petKey, Uri.parse(buddyInfo.photo)) {
                    downloadUrl ->
                    buddy.photo = downloadUrl.toString()

                    childUpdates.put(petPath, buddy.toMap())
                    updateDB(childUpdates)
                }
            } else {
                childUpdates.put(petPath, buddy.toMap())
                updateDB(childUpdates)
            }

        } else {
            Log.d(TAG, "Error, new pet found")
        }
    }

    fun addPetOwnerRequestObservable(baseTag: BaseTag): Observable<Boolean> = Observable.create {
        subscriber ->
        if(baseTag.petId.isNotEmpty()) {
            Log.d(TAG, "Adding owner pet")

            getPetReference(baseTag.petId).child(Buddy.DATABASE_OWNS_CHILD).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    Log.d(TAG, "Error, owner pet cancelled " + error?.message)
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if(dataSnapshot != null && dataSnapshot.hasChildren()) {
                        if (dataSnapshot.children.any { it.key == getCurrentUserUID() }) {
                            subscriber.onNext(true)
                        } else {
                            subscriber.onNext(false)
                            val requestKey = getDatabaseReference(DATABASE_REQUESTS_PATH).push().key
                            val request = Request(getCurrentUserUID(), baseTag.petId)
                            val childUpdates = HashMap<String, Any?>()

                            with(childUpdates) {
                                put(DATABASE_REQUESTS_PATH + requestKey, request.toMap())
                            }

                            updateDB(childUpdates)
                        }
                        subscriber.onComplete()
                    }
                }
            })

        } else {
            Log.d(TAG, "Error, owner pet not found")
        }
    }

    fun allowPetOwner(request: Request, key: String, allow: Boolean) {
        Log.d(TAG, "Allowing owner pet")
        getUserRequestsReference(getCurrentUserUID()).child(key).setValue(null)

        getPetReference(request.petId).child(Buddy.DATABASE_OWNS_CHILD).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Log.d(TAG, "Error, owner pet allow cancelled " + error?.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val childUpdates = HashMap<String, Any?>()
                val userPath = DATABASE_USERS_PATH + request.username + "/"
                val petPath = DATABASE_PETS_PATH + request.petId + "/"

                with(childUpdates) {
                    if(allow) {
                        put(userPath + User.DATABASE_OWNS_CHILD + "/" + request.petId, true)
                        put(petPath + Buddy.DATABASE_OWNS_CHILD + "/" + request.username, true)
                    }
                    put(DATABASE_REQUESTS_PATH + key, null)
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
                put(currentUserPath + User.DATABASE_FOLLOWS_CHILD + "/" + baseTag.petId, true)
                put(currentPetPath + Buddy.DATABASE_FOLLOWS_CHILD + "/" + getCurrentUserUID(), true)
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

            getPetReference(petId).child(Buddy.DATABASE_OWNS_CHILD).addListenerForSingleValueEvent(object: ValueEventListener {
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
            getTagsReference().orderByChild(BaseTag.DATABASE_PETID_CHILD).equalTo(petId).addListenerForSingleValueEvent(object: ValueEventListener {
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

            childUpdates.put(currentUserPath + Buddy.DATABASE_NAME_CHILD, buddy.name)
            childUpdates.put(currentUserPath + Buddy.DATABASE_ANIMAL_CHILD, buddy.animal)
            childUpdates.put(currentUserPath + Buddy.DATABASE_BREED_CHILD, buddy.breed)
            childUpdates.put(currentUserPath + Buddy.DATABASE_PHOTO_CHILD, buddy.photo)

            updateDB(childUpdates)
        }
    }

    // Places API
    fun getPlaceReference(placeId: String) = getPlacesReference().child(placeId)
    fun getPlacesReference() = getDatabaseReference(DATABASE_PLACES_PATH)
    fun getUserPlacesReference(username: String) = getUserReference(username).child(User.DATABASE_PLACES_CHILD)
    fun addPlace(place: Place) {
        Log.d(TAG, "Adding new place")
        val placeKey = getPlacesReference().push().key
        val childUpdates = HashMap<String, Any?>()

//        val itemKey1 = getFriendlyItemsReference().push().key
//        val itemKey2 = getFriendlyItemsReference().push().key
//        val itemKey3 = getFriendlyItemsReference().push().key
//
//        with(childUpdates) {
//            put(DATABASE_FITEMS_PATH + itemKey1, FriendlyItem("Snacks", "Description", FriendlyItem.TYPE_FOOD).toMap())
//            put(DATABASE_FITEMS_PATH + itemKey2, FriendlyItem("Water", "Description", FriendlyItem.TYPE_WATER).toMap())
//            put(DATABASE_FITEMS_PATH + itemKey3, FriendlyItem("Reserved Place", "Description", FriendlyItem.TYPE_PLACE).toMap())
//        }
//
//        place.items = mapOf(
//                Pair(itemKey1, true),
//                Pair(itemKey2, true),
//                Pair(itemKey3, true))

        if(place.photo.isNotEmpty()) {
            uploadPlaceFile(placeKey, Uri.parse(place.photo)) {
                downloadUrl ->
                place.photo = downloadUrl.toString()

                childUpdates.put(DATABASE_PLACES_PATH + placeKey, place.toMap())
                updateDB(childUpdates)
            }
        } else {
            childUpdates.put(DATABASE_PLACES_PATH + placeKey, place.toMap())
            updateDB(childUpdates)
        }
    }

    // Friendly Items API
    fun getFriendlyItemsReference() = getDatabaseReference(DATABASE_FITEMS_PATH)
    fun getFriendlyItemReference(fitemId: String) = getFriendlyItemsReference().child(fitemId)
    fun getPlaceFriendlyItemsReference(placeId: String) = getPlaceReference(placeId).child(Place.DATABASE_ITEMS_CHILD)

    // Animals API
    fun getAnimalsReference() = getDatabaseReference(DATABASE_ANIMALS_PATH)
    fun getAnimalBreedsReference(animal: String) = getAnimalsReference().child(animal)

    // Storage API
    fun uploadPersonalFile(path: Uri, onSuccess: (Uri) -> Unit) {
        firebaseStoreService.getUserStorageReference(getCurrentUserUID()).child(path.lastPathSegment).putFile(path)
                .addOnSuccessListener {
                    val downloadUrl = it.downloadUrl

                    if(downloadUrl != null) {
                        onSuccess(downloadUrl)
                    }
                }
    }

    fun uploadPetFile(petId: String, path: Uri, onSuccess: (Uri) -> Unit) {
        firebaseStoreService.getPetStorageReference(petId).child(path.lastPathSegment).putFile(path)
                .addOnSuccessListener {
                    val downloadUrl = it.downloadUrl

                    if(downloadUrl != null) {
                        onSuccess(downloadUrl)
                    }
                }
    }

    fun uploadPlaceFile(placeId: String, path: Uri, onSuccess: (Uri) -> Unit) {
        firebaseStoreService.getPlaceStorageService(placeId).child(path.lastPathSegment).putFile(path)
                .addOnSuccessListener {
                    val downloadUrl = it.downloadUrl

                    if(downloadUrl != null) {
                        onSuccess(downloadUrl)
                    }
                }
    }

    fun uploadPostFile(postId: String, path: Uri, onSuccess: (Uri) -> Unit) {
        firebaseStoreService.getPostStorageService(postId).child(path.lastPathSegment).putFile(path)
                .addOnSuccessListener {
                    val downloadUrl = it.downloadUrl

                    if(downloadUrl != null) {
                        onSuccess(downloadUrl)
                    }
                }
    }

    // Requests API
    fun getRequestsReference() = getDatabaseReference(DATABASE_REQUESTS_PATH)
    fun getRequestReference(requestId: String) = getRequestsReference().child(requestId)
    fun getUserRequestsReference(username: String) = getUserReference(username).child(User.DATABASE_REQUESTS_CHILD)

    // Posts API
    fun getPostsReference() = getDatabaseReference(DATABASE_POSTS_PATH)
    fun getPostReference(postId: String) = getPostsReference().child(postId)
    fun getPetPostsReference(petId: String) = getPetReference(petId).child(Buddy.DATABASE_POSTS_CHILD)
    fun getUserPostsReference(username: String) = getUserReference(username).child(User.DATABASE_POSTS_CHILD)
    fun addPost(post: Post) {
        Log.d(TAG, "Adding new post")
        val postKey = getPostsReference().push().key
        val childUpdates = HashMap<String, Any?>()

        if(post.photo.isNotEmpty()) {
            uploadPlaceFile(postKey, Uri.parse(post.photo)) {
                downloadUrl ->
                post.photo = downloadUrl.toString()

                childUpdates.put(DATABASE_POSTS_PATH + postKey, post.toMap())
            }
        } else {
            childUpdates.put(DATABASE_POSTS_PATH + postKey, post.toMap())
        }

        childUpdates.put(DATABASE_PETS_PATH + post.petId + "/" + Buddy.DATABASE_POSTS_CHILD + "/" + postKey, true)

        updateDB(childUpdates)
    }

    fun addPostLike(postId: String) {
        val childUpdates = HashMap<String, Any?>()

            childUpdates.put(DATABASE_POSTS_PATH + postId + "/" + Post.DATABASE_LIKES_CHILD + "/" + getCurrentUserUID(), true)
            updateDB(childUpdates)
    }

    fun removePostLike(postId: String) {
        val childUpdates = HashMap<String, Any?>()

        childUpdates.put(DATABASE_POSTS_PATH + postId + "/" + Post.DATABASE_LIKES_CHILD + "/" + getCurrentUserUID(), null)
        updateDB(childUpdates)
    }

    fun queryBuddies() = getUserPetsReference(getCurrentUserUID())
    fun queryFollow() = getUserFollowReference(getCurrentUserUID())
    fun queryRequests() = getUserRequestsReference(getCurrentUserUID())
    fun queryPlaces() = getUserPlacesReference(getCurrentUserUID())
    fun queryPosts() = getUserPostsReference(getCurrentUserUID())
}