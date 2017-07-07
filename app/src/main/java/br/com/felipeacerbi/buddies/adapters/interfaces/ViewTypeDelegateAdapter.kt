package br.com.felipeacerbi.buddies.adapters.interfaces

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
interface ViewTypeDelegateAdapter {
    fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType)
    fun cleanUp()
}