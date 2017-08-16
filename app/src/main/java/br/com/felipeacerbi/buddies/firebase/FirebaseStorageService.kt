package br.com.felipeacerbi.buddies.firebase

import com.google.firebase.storage.FirebaseStorage

class FirebaseStorageService {

    val firebaseStorage = FirebaseStorage.getInstance()

    fun getStorageReference(path: String) = firebaseStorage.getReference(path)

    fun getUsersStorageReference() = getStorageReference("users")
    fun getUserStorageReference(username: String) = getUsersStorageReference().child(username)

    fun getPetsStorageReference() = getStorageReference("pets")
    fun getPetStorageReference(petId: String) = getPetsStorageReference().child(petId)

    fun getPlacesStorageReference() = getStorageReference("places")
    fun getPlaceStorageService(placeId: String) = getPlacesStorageReference().child(placeId)

    fun getPostsStorageReference() = getStorageReference("posts")
    fun getPostStorageService(postId: String) = getPostsStorageReference().child(postId)
}