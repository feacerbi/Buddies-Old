package br.com.felipeacerbi.buddies.tags

import android.content.Context
import android.util.Log
import br.com.felipeacerbi.buddies.firebase.FirebaseService
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by felipe.acerbi on 18/07/2017.
 */
class TagsManager(val context: Context) {

    val firebaseService = FirebaseService()

    companion object {
        val TAG = "TagsManager"
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

}