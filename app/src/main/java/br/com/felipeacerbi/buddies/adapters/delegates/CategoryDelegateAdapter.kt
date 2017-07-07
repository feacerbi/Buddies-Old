package br.com.felipeacerbi.buddies.adapters.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.models.Category
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewTypeDelegateAdapter
import br.com.felipeacerbi.buddies.utils.inflate
import kotlinx.android.synthetic.main.category_list_item.view.*

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
class CategoryDelegateAdapter : ViewTypeDelegateAdapter {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return CategoryViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        holder as CategoryViewHolder
        item as Category

        with(holder.itemView) {
            title.text = item.title
        }
    }

    override fun cleanUp() {
        // No need
    }

    inner class CategoryViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.category_list_item))

}