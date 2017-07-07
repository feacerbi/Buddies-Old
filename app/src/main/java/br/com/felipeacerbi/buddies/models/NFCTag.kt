package br.com.felipeacerbi.buddies.models

import android.nfc.NdefMessage
import android.nfc.Tag

/**
 * Created by felipe.acerbi on 05/07/2017.
 */
data class NFCTag(
        val tag: Tag?,
        val ndefMessage: NdefMessage?,
        val id: String?,
        val decodedPayload: String?,
        var petId: String = "",
        var petName: String = "") {
}