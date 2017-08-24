package br.com.felipeacerbi.buddies.adapters.listeners

import android.content.Context
import android.view.LayoutInflater
import kotlin.reflect.KClass

interface IListClickListener {
    fun <T : Any> onListClick(clazz: KClass<T>, identifiers: Array<String>?, extras: Array<Any>?)
    fun getContext(): Context
    fun getViewInflater(): LayoutInflater
    fun selectListItem(position: Int)
}