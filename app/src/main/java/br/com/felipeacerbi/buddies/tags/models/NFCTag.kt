package br.com.felipeacerbi.buddies.tags.models

import android.nfc.NdefMessage
import android.nfc.Tag
import org.parceler.Parcel

@Parcel
data class NFCTag(
        val tag: Tag?,
        val message: NdefMessage?,
        val payload: String = "",
        val baseTag: BaseTag)