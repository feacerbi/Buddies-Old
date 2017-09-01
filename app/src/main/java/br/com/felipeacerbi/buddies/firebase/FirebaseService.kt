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

class FirebaseService : FirebaseInstanceIdService() {

    companion object {
        val TAG = "FirebaseService"
        val DATABASE_USERS_PATH = "users/"
        val DATABASE_PETS_PATH = "pets/"
        val DATABASE_TAGS_PATH = "tags/"
        val DATABASE_REQUESTS_PATH = "requests/"
        val DATABASE_SUGGESTIONS_PATH = "suggestions/"
        val DATABASE_PLACES_PATH = "places/"
        val DATABASE_FITEMS_PATH = "fitems/"
        val DATABASE_ANIMALS_PATH = "animals/"
        val DATABASE_POSTS_PATH = "posts/"
        val DATABASE_COMMENTS_PATH = "comments/"
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
    fun removeUserLocation() = getUserReference(getCurrentUserUID()).child(User.DATABASE_LATLONG_CHILD).setValue(null)
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
            user.idToken = getAppIDToken()

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"

            childUpdates.put(currentUserPath + User.DATABASE_NAME_CHILD, user.name)
            childUpdates.put(currentUserPath + User.DATABASE_EMAIL_CHILD, user.email)
            childUpdates.put(currentUserPath + User.DATABASE_PHOTO_CHILD, user.photo)
            childUpdates.put(currentUserPath + User.DATABASE_IDTOKEN_CHILD, user.idToken)

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

    fun addNewTag(baseTag: BaseTag) {
        val tagKey = getTagsReference().push().key

        val childUpdates = HashMap<String, Any?>()
        childUpdates.put(DATABASE_TAGS_PATH + tagKey, baseTag.toMap())

        updateDB(childUpdates)
    }

    fun checkTagObservable(baseTag: BaseTag): Observable<Pair<String, BaseTag>> = Observable.create {
        subscriber ->
        getTagsReference().orderByChild(BaseTag.DATABASE_ID_CHILD).equalTo(baseTag.id).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                Log.d(TAG, "Adding pet cancelled: " + error.toString())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(dataSnapshot != null && dataSnapshot.childrenCount > 0) {
                    val result = dataSnapshot.children.first()
                    val foundTag = BaseTag(result)

                    subscriber.onNext(Pair(result.key, foundTag))
                    subscriber.onComplete()
                    Log.d(TAG, "Tag found")
                } else {
                    subscriber.onError(Throwable("Tag not found"))
                    Log.d(TAG, "Tag not found")
                }
            }
        })
    }

    // Pets API
    fun getPetReference(petId: String) = getPetsReference().child(petId)
    fun getPetsReference() = getDatabaseReference(DATABASE_PETS_PATH)
    fun getUserPetsReference(username: String) = getUserReference(username).child(User.DATABASE_OWNS_CHILD)
    fun getUserFollowReference(username: String) = getUserReference(username).child(User.DATABASE_FOLLOWS_CHILD)

    fun addNewPet(tagKey: String, baseTag: BaseTag, buddy: Buddy) {
        Log.d(TAG, "Adding new pet")
        val petKey = getPetsReference().push().key

        buddy.tagId = baseTag.id

        (buddy.owners as HashMap).put(getCurrentUserUID(), true)

        val childUpdates = HashMap<String, Any?>()
        val currentUserPath = DATABASE_USERS_PATH + getCurrentUserUID() + "/"
        val tagPath = DATABASE_TAGS_PATH + tagKey + "/"
        val petPath = DATABASE_PETS_PATH + petKey + "/"

        with(childUpdates) {
            put(currentUserPath + User.DATABASE_OWNS_CHILD + "/" + petKey, true)
            put(tagPath + BaseTag.DATABASE_PETID_CHILD, petKey)
        }

        if(buddy.photo.isNotEmpty()) {
            uploadPetFile(petKey, Uri.parse(buddy.photo)) {
                downloadUrl ->
                buddy.photo = downloadUrl.toString()

                childUpdates.put(petPath, buddy.toMap())
                updateDB(childUpdates)
            }
        } else {
            childUpdates.put(petPath, buddy.toMap())
            updateDB(childUpdates)
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

    // Suggestions API
    fun getSuggestionReference(placeId: String) = getSuggestionsReference().child(placeId)
    fun getSuggestionsReference() = getDatabaseReference(DATABASE_SUGGESTIONS_PATH)
    fun getUserSuggestionsReference(username: String) = getUserReference(username).child(User.DATABASE_PLACES_CHILD)
    fun removeUserSuggestions() = getUserPlacesReference(getCurrentUserUID()).setValue(null)

    fun addSuggestion(place: Place) {
        Log.d(TAG, "Adding new place suggestion")
        val suggestionKey = getSuggestionsReference().push().key

        val suggestion = Suggestion(
                getCurrentUserUID(),
                place)

        val childUpdates = HashMap<String, Any?>()
        childUpdates.put(DATABASE_SUGGESTIONS_PATH + suggestionKey, suggestion.toMap())
        updateDB(childUpdates)
    }

    // Places API
    fun getPlaceReference(placeId: String) = getPlacesReference().child(placeId)
    fun getPlacesReference() = getDatabaseReference(DATABASE_PLACES_PATH)
    fun getUserPlacesReference(username: String) = getUserReference(username).child(User.DATABASE_PLACES_CHILD)
    fun removeUserPlaces() = getUserPlacesReference(getCurrentUserUID()).setValue(null)

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

    fun removePetPost(petId: String, postId: String) {
        getPetPostsReference(petId).child(postId).setValue(null)
        getPostReference(postId).setValue(null)
    }

    fun addPost(post: Post) {
        Log.d(TAG, "Adding new post")
        val postKey = getPostsReference().push().key
        val childUpdates = HashMap<String, Any?>()

        childUpdates.put(DATABASE_PETS_PATH + post.petId + "/" + Buddy.DATABASE_POSTS_CHILD + "/" + postKey, true)

        if(post.photo.isNotEmpty()) {
            uploadPostFile(postKey, Uri.parse(post.photo)) {
                downloadUrl ->
                post.photo = downloadUrl.toString()

                childUpdates.put(DATABASE_POSTS_PATH + postKey, post.toMap())
                updateDB(childUpdates)
            }
        } else {
            childUpdates.put(DATABASE_POSTS_PATH + postKey, post.toMap())
            updateDB(childUpdates)
        }
    }

    fun updatePost(post: Post?, postId: String) {
        if(post != null) {

            val childUpdates = HashMap<String, Any?>()
            val currentUserPath = DATABASE_POSTS_PATH + postId + "/"

            childUpdates.put(currentUserPath + Post.DATABASE_MESSAGE_CHILD, post.message)
            childUpdates.put(currentUserPath + Post.DATABASE_LOCATION_CHILD, post.location)

            if(post.photo.isNotEmpty()) {
                uploadPostFile(postId, Uri.parse(post.photo)) {
                    downloadUrl ->
                    post.photo = downloadUrl.toString()

                    childUpdates.put(currentUserPath + Post.DATABASE_PHOTO_CHILD, post.photo)
                    updateDB(childUpdates)
                }
            } else {
                updateDB(childUpdates)
            }
        }
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

    // Comments API
    fun getCommentsReference() = getDatabaseReference(DATABASE_COMMENTS_PATH)
    fun getCommentReference(commentId: String) = getCommentsReference().child(commentId)
    fun getPostCommentsReference(postId: String) = getPostReference(postId).child(Post.DATABASE_COMMENTS_CHILD)
    fun addComment(comment: Comment) {
        Log.d(TAG, "Adding new comment")
        val commentKey = getCommentsReference().push().key
        val childUpdates = HashMap<String, Any?>()

        comment.posterId = getCurrentUserUID()

        childUpdates.put(DATABASE_COMMENTS_PATH + commentKey, comment.toMap())
        childUpdates.put(DATABASE_POSTS_PATH + comment.postId + "/" + Post.DATABASE_COMMENTS_CHILD + "/" + commentKey, true)

        updateDB(childUpdates)
    }

    fun addCommentLike(commentId: String) {
        val childUpdates = HashMap<String, Any?>()

        childUpdates.put(DATABASE_COMMENTS_PATH + commentId + "/" + Comment.DATABASE_LIKES_CHILD + "/" + getCurrentUserUID(), true)
        updateDB(childUpdates)
    }

    fun removeCommentLike(commentId: String) {
        val childUpdates = HashMap<String, Any?>()

        childUpdates.put(DATABASE_COMMENTS_PATH + commentId + "/" + Comment.DATABASE_LIKES_CHILD + "/" + getCurrentUserUID(), null)
        updateDB(childUpdates)
    }

    fun queryBuddies() = getUserPetsReference(getCurrentUserUID())
    fun queryFollow() = getUserFollowReference(getCurrentUserUID())
    fun queryRequests() = getUserRequestsReference(getCurrentUserUID())
    fun queryPlaces() = getUserPlacesReference(getCurrentUserUID())
    fun queryPosts() = getUserPostsReference(getCurrentUserUID())
}