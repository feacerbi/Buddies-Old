package br.com.felipeacerbi.buddies.activities.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by felipe.acerbi on 19/07/2017.
 */
abstract class RxBaseActivity : AppCompatActivity() {

    var subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        subscriptions = CompositeDisposable()
    }

    override fun onPause() {
        super.onPause()
        if(!subscriptions.isDisposed) subscriptions.dispose()
        subscriptions.clear()
    }

}