package br.com.felipeacerbi.buddies.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable

open class RxBaseActivity : AppCompatActivity() {

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
