package br.com.felipeacerbi.buddies.adapters.interfaces

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
interface ViewTypeDelegateAdapter {
    fun onCreateDelegateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    fun onBindDelegateViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: ViewType? = null)
    fun cleanUp()
}