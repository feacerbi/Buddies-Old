package br.com.felipeacerbi.buddies.adapters

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import br.com.felipeacerbi.buddies.FirebaseService
import br.com.felipeacerbi.buddies.adapters.delegates.BuddyDelegateAdapter
import br.com.felipeacerbi.buddies.adapters.delegates.CategoryDelegateAdapter
import br.com.felipeacerbi.buddies.adapters.interfaces.IOnListFragmentInteractionListener
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewTypeDelegateAdapter

import br.com.felipeacerbi.buddies.models.Category
import br.com.felipeacerbi.buddies.utils.Constants
import br.com.felipeacerbi.buddies.utils.forEach
import javax.inject.Inject

/**
 * [RecyclerView.Adapter] that can display a list and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class BuddiesAdapter(private var items: ArrayList<ViewType> = ArrayList<ViewType>(),
                     private val firebaseService: FirebaseService,
                     private val listener: IOnListFragmentInteractionListener?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val delegateAdapters by lazy {
        SparseArrayCompat<ViewTypeDelegateAdapter>()
    }

    init {
        delegateAdapters.put(Constants.CATEGORY_VIEW_TYPE, CategoryDelegateAdapter())
        delegateAdapters.put(Constants.BUDDY_VIEW_TYPE, BuddyDelegateAdapter(firebaseService.getUserPetsReference(firebaseService.getCurrentUsername())))
        items.add(Category("Buddies"))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegateAdapters.get(viewType).onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegateAdapters.get(getItemViewType(position)).onBindViewHolder(holder, items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].getViewType()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun cleanAdapters() {
        delegateAdapters.forEach { it.cleanUp() }
    }

    fun addItem(item: ViewType) {
        items.add(item)
        notifyItemInserted(items.indexOf(item))
    }

    fun addItems(newItems: Array<ViewType>) {
        items.addAll(newItems)
        notifyItemRangeChanged(items.indexOf(newItems[0]), items.indexOf(newItems[newItems.size - 1]))
    }
}
