package br.com.felipeacerbi.buddies.models

import br.com.felipeacerbi.buddies.utils.Constants
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
data class Category(val title: String) : ViewType {

    override fun getViewType(): Int {
        return Constants.CATEGORY_VIEW_TYPE
    }
}