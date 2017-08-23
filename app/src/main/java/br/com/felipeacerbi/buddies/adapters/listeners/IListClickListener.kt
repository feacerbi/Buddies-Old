package br.com.felipeacerbi.buddies.adapters.listeners

import android.content.Context
import android.view.LayoutInflater

interface IListClickListener {
    fun onListClick(identifiers: Array<Any>?)
    fun getContext(): Context
    fun getViewInflater(): LayoutInflater
    fun selectListItem(position: Int)
}