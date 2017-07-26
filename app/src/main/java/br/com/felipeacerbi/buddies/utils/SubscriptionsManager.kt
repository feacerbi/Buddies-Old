package br.com.felipeacerbi.buddies.utils

import android.content.Context
import android.util.Log
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.models.User
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by felipe.acerbi on 18/07/2017.
 */
class SubscriptionsManager(val context: Context) {

    val firebaseService = FirebaseService()

    companion object {
        val TAG = "SubscriptionsManager"
    }

    fun checkTagWithActionSubscription(baseTag: BaseTag,
                                       existsAction: (BaseTag) -> Unit,
                                       notExistsAction: (BaseTag) -> Unit): Disposable {
        Log.d(TAG, "Check pet with action " + baseTag.id)
        return firebaseService.checkTagObservable(baseTag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { foundTag ->
                            if(foundTag.petId.isEmpty()) {
                                Log.d(TAG, "Tag not found")
                                notExistsAction(foundTag)
                            } else {
                                Log.d(TAG, "Tag found")
                                existsAction(foundTag)
                            }
                        },
                        { e -> Log.d(TAG, "Error adding pet " + e.message) })
    }

    fun checkUserWithActionSubscription(existsAction: (Pair<Boolean, User>) -> Unit,
                                        notExistsAction: (Pair<Boolean, User>) -> Unit): Disposable {
        val username = firebaseService.getCurrentUserUID()
        Log.d(TAG, "Check user with action " + username)

        return firebaseService.checkUserObservable(username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { pair ->
                            if(pair.first) {
                                existsAction(pair)
                                Log.d(TAG, "Tag found")
                            } else {
                                notExistsAction(pair)
                                Log.d(TAG, "Tag not found")
                            }
                        },
                        { e -> Log.d(TAG, "Error adding pet " + e.message) })
    }

}