package br.com.felipeacerbi.buddies.models

import br.com.felipeacerbi.buddies.utils.Constants
import br.com.felipeacerbi.buddies.adapters.interfaces.ViewType

/**
 * Created by felipe.acerbi on 04/07/2017.
 */
data class Buddy(
        var id: String,
        var name: String,
        var breed: String,
        var tag: String,
        var owners: HashMap<String, Boolean>) : ViewType {

    override fun getViewType(): Int {
        return Constants.BUDDY_VIEW_TYPE
    }

}