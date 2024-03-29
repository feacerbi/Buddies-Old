package br.com.felipeacerbi.buddies.utils

import android.content.Context
import android.util.Log
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SubscriptionsManager(val context: Context) {

    val firebaseService = FirebaseService()

    companion object {
        val TAG = "SubscriptionsManager"
    }

    fun checkTagSubscription(baseTag: BaseTag,
                             usedAction: (BaseTag) -> Unit,
                             newAction: (String, BaseTag) -> Unit,
                             notVerifiedAction: (BaseTag) -> Unit): Disposable {
        return firebaseService.checkTagObservable(baseTag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { tagPair ->
                            val key = tagPair.first
                            val foundTag = tagPair.second

                            if(foundTag.verified) {
                                if (foundTag.petId.isEmpty()) {
                                    Log.d(TAG, "New Tag")
                                    newAction(key, foundTag)
                                } else {
                                    Log.d(TAG, "Used Tag")
                                    usedAction(foundTag)
                                }
                            } else {
                                Log.d(TAG, "Not verified Tag")
                                notVerifiedAction(foundTag)
                            }
                        },
                        { e -> Log.d(TAG, "Error adding pet: " + e.message) })
    }

    fun checkOwnerRequestSubscription(baseTag: BaseTag,
                                      ownsAction: () -> Unit,
                                      notOwnsAction: () -> Unit): Disposable {
        return firebaseService.addPetOwnerRequestObservable(baseTag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { isOwner ->
                            if(isOwner) {
                                Log.d(TAG, "Is already owner")
                                ownsAction()
                            } else {
                                Log.d(TAG, "Is not owner")
                                notOwnsAction()
                            }
                        },
                        { e -> Log.d(TAG, "Error adding pet " + e.message) })
    }

    fun checkUserSubscription(existsAction: (Pair<Boolean, User>) -> Unit,
                              notExistsAction: (Pair<Boolean, User>) -> Unit): Disposable {
        val username = firebaseService.getCurrentUserUID()

        return firebaseService.checkUserObservable(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { pair ->
                            if(pair.first) {
                                existsAction(pair)
                                Log.d(TAG, "User found")
                            } else {
                                notExistsAction(pair)
                                Log.d(TAG, "User not found")
                            }
                        },
                        { e -> Log.d(TAG, "Error adding user " + e.message) })
    }

}