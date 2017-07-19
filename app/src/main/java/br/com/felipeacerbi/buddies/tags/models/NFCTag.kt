package br.com.felipeacerbi.buddies.tags.models

import android.nfc.NdefMessage
import android.nfc.Tag

/**
 * Created by felipe.acerbi on 11/07/2017.
 */
data class NFCTag(
        val tag: Tag?,
        val message: NdefMessage?,
        val payload: String = "",
        val baseTag: BaseTag)