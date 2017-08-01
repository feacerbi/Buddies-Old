package br.com.felipeacerbi.buddies.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.fragments.FirebaseListFragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.Query
import java.util.*
import kotlin.reflect.KClass

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun ByteArray.toHexString(): String {
    var i: Int
    var j = 0
    var `in`: Int
    val hex = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F")
    var out = ""

    while (j < this.size) {
        `in` = this[j].toInt() and 0xff
        i = `in` shr 4 and 0x0f
        out += hex[i]
        i = `in` and 0x0f
        out += hex[i]
        j++
    }
    return out
}

fun String.toFormatedDate(): String {
    val reqTime = Calendar.getInstance()
    reqTime.timeInMillis = this.toLong()
    return reqTime.time.toString().substringBeforeLast(" GMT")
}

fun AlertDialog.Builder.showOneChoiceCancelableDialog(
        title: String,
        message: String,
        buttonTitle: String,
        func: (DialogInterface, Int) -> Unit) {
    setTitle(title)
    setMessage(message)
    setPositiveButton(buttonTitle, func)
    setNeutralButton("Cancel") { _, _ -> }
    show()
}

fun AlertDialog.Builder.showTwoChoiceCancelableDialog(
        title: String,
        message: String,
        buttonOneTitle: String,
        buttonTwoTitle: String,
        funcOne: (DialogInterface, Int) -> Unit,
        funcTwo: (DialogInterface, Int) -> Unit) {
    setTitle(title)
    setMessage(message)
    setPositiveButton(buttonOneTitle, funcOne)
    setNegativeButton(buttonTwoTitle, funcTwo)
    setNeutralButton("Cancel") { _, _ ->  }
    show()
}

fun AlertDialog.Builder.showInputDialog(
        title: String,
        buttonTitle: String,
        inputView: View,
        func: (DialogInterface, Int) -> Unit) {
    setView(inputView)
    setTitle(title)
    setPositiveButton(buttonTitle, func)
    setNeutralButton("Cancel") { _, _ -> }
    show()
}

fun FloatingActionButton.setUp(context: Context, show: Boolean, resource: Int) {
    if(show) {
        visibility = View.VISIBLE
        setImageDrawable(resources.getDrawable(resource, context.theme))
    } else {
        visibility = View.GONE
    }
}

fun RecyclerView.getFirebaseAdapter(): FirebaseRecyclerAdapter<*, *>? {
    if(adapter != null) {
        return adapter as FirebaseRecyclerAdapter<*, *>
    }
    return null
}

fun Bundle.makeQueryBundle(context: Context, query: Query): Bundle {
    putString(FirebaseListFragment.DATABASE_REFERENCE, query.toString().removePrefix(context.getString(R.string.firebase_query_prefix)))
    return this
}

fun Fragment.transact(activity: AppCompatActivity, id: Int, bundle: Bundle? = null) {
    val transaction = activity.supportFragmentManager.beginTransaction()

    if(bundle != null) arguments = bundle

    transaction.replace(id, this)
    transaction.commit()
}

fun <T : Any> Activity.launchActivity(clazz: KClass<T>) {
    val intent = Intent(this, clazz.java)
    startActivity(intent)
}

fun <T : Any> Activity.launchActivityWithExtras(clazz: KClass<T>, identifiers: Array<String>, extras: Array<Any>) {
    val intent = Intent(this, clazz.java)

    for(extra in extras) {
        if(extra is String) {
            intent.putExtra(identifiers[extras.indexOf(extra)], extra)
        } else if(extra is Int) {
            intent.putExtra(identifiers[extras.indexOf(extra)], extra)
        } else if(extra is Boolean) {
            intent.putExtra(identifiers[extras.indexOf(extra)], extra)
        }
    }

    startActivity(intent)
}

fun <T : Any> Activity.launchActivityForResult(clazz: KClass<T>, identifier: Int) {
    val intent = Intent(this, clazz.java)
    startActivityForResult(intent, identifier)
}

fun <T : Any> Activity.launchActivityAndFinish(clazz: KClass<T>) {
    launchActivity(clazz)
    finish()
}

