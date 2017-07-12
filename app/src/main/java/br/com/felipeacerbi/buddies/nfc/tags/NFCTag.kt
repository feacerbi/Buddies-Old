package br.com.felipeacerbi.buddies.nfc.tags

import android.nfc.NdefMessage
import android.nfc.Tag
import java.io.Serializable

/**
 * Created by felipe.acerbi on 11/07/2017.
 */
data class NFCTag(
        val tag: Tag?,
        val message: NdefMessage?,
        val payload: String = "",
        val baseTag: BaseTag)