package br.com.felipeacerbi.buddies.tags

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.tags.models.NFCTag
import br.com.felipeacerbi.buddies.utils.toHexString
import java.io.UnsupportedEncodingException
import kotlin.experimental.and

class NFCService {

    companion object {
        val TAG = "NFCService"
    }

    fun parseIntent(intent: Intent): NFCTag {

        val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        var message: NdefMessage? = null

        if (messages != null && messages.isNotEmpty()) {
            message = messages[0] as NdefMessage
            Log.d(TAG, "Ndef message: " + message)
        }

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        Log.d(TAG, "Ndef tagId: " + tag)

        val id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID).toHexString()
        Log.d(TAG, "Ndef id: " + id)

        return NFCTag(tag, message, decodePayload(message), BaseTag(id))
    }

    fun isNFCSupported(context: Context) = NfcAdapter.getDefaultAdapter(context) != null

    private fun decodePayload(ndefMessage: NdefMessage?): String {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        // Get the Text
        var resultPayload = ""

        if(ndefMessage != null) {
            val payload = ndefMessage.records[0].payload

            // Get the Text Encoding
            val textEncoding = /*if ((payload[0] and 128).equals(0)) "UTF-8" else*/ "UTF-8"

            // Get the Language Code
            val languageCodeLength = payload[0] and 51

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            try {
                resultPayload = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, charset(textEncoding))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

        return resultPayload
    }

    fun activateForegroundDispatchSystem(activity: AppCompatActivity) {
        if(isNFCSupported(activity)) {
            NfcAdapter.getDefaultAdapter(activity).enableForegroundDispatch(
                    activity,
                    createPendingIntent(activity),
                    arrayOf(getIntentFilters()),
                    getTechs())
        }
    }

    fun deactivateForegroundDispatchSystem(activity: AppCompatActivity) {
        if(isNFCSupported(activity)) {
            NfcAdapter.getDefaultAdapter(activity).disableForegroundDispatch(activity)
        }
    }

    fun createPendingIntent(activity: AppCompatActivity): PendingIntent {
        return PendingIntent.getActivity(activity, 0, Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
    }

    fun getIntentFilters(): IntentFilter {
        val intentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        intentFilter.addDataType("text/plain")
        return intentFilter
    }

    fun getTechs() = arrayOf(arrayOf(
            IsoDep::javaClass.name,
            NfcA::javaClass.name,
            NfcB::javaClass.name,
            NfcF::javaClass.name,
            NfcV::javaClass.name,
            Ndef::javaClass.name,
            NdefFormatable::javaClass.name,
            MifareClassic::javaClass.name,
            MifareUltralight::javaClass.name
    ))
}