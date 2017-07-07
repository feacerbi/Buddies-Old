package br.com.felipeacerbi.buddies.utils

import android.support.v4.util.SparseArrayCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewTypeDelegateAdapter

/**
 * Created by felipe.acerbi on 04/07/2017.
 */

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

fun String.toUsername(): String {
    return this.toLowerCase().trim().replace(".", "", true).replace("@", "", true)
}

fun SparseArrayCompat<ViewTypeDelegateAdapter>.forEach(func: (adapter: ViewTypeDelegateAdapter) -> Unit) {
    if(size() > 0) {
        var i = 0
        while(i < size()) {
            func(get(i))
            i++
        }
    }
}