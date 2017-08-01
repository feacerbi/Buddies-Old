package br.com.felipeacerbi.buddies.adapters.listeners

import android.content.Context

interface IListClickListener {
    fun onListClick(identifiers: Array<Any>)
    fun getContext(): Context
}