package br.com.felipeacerbi.buddies.activities.base

import android.support.v7.app.AppCompatActivity
import br.com.felipeacerbi.buddies.utils.SubscriptionsManager
import io.reactivex.disposables.CompositeDisposable

abstract class RxBaseActivity : AppCompatActivity() {

    var subscriptions = CompositeDisposable()

    val subscriptionsManager: SubscriptionsManager by lazy {
        SubscriptionsManager(this)
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