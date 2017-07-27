package br.com.felipeacerbi.buddies.adapters.listeners

import android.content.Context

/**
 * Created by feaac on 7/26/2017.
 */
interface IListClickListener {
    fun onListClick(identifier: String)
    fun getContext(): Context
}