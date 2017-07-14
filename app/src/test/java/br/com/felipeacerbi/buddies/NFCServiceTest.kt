package br.com.felipeacerbi.buddies

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.Tag
import org.junit.Test
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.util.Log
import br.com.felipeacerbi.buddies.nfc.NFCService
import java.util.*
import org.junit.Assert
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.api.mockito.PowerMockito.*
import org.powermock.modules.junit4.PowerMockRunner
import java.nio.charset.Charset
import android.icu.util.ULocale.getLanguage
import br.com.felipeacerbi.buddies.utils.toHexString


/**
 * Created by felipe.acerbi on 14/07/2017.
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(Log::class)
class NFCServiceTest {

    val testPayload = "AAAAA"
    val testId = "testid123".toByteArray(charset("UTF-8"))
    val tagDescription = "testtag"

    @Before
    fun setUpMocks() {
        mockStatic(Log::class.java)
    }

    @Test
    fun test_parseNFCTag() {
        val nfcService = NFCService()

        val testTag = mock(Tag::class.java)
        `when`(testTag.toString()).thenReturn(tagDescription)

        val ndefRecord = mock(NdefRecord::class.java)
        `when`(ndefRecord.payload).thenReturn(createTextRecord(testPayload))

        val ndefMessage = mock(NdefMessage::class.java)
        `when`(ndefMessage.records).thenReturn(arrayOf(ndefRecord))

        val testIntent = mock(Intent::class.java)
        `when`(testIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)).thenReturn(arrayOf(ndefMessage))
        `when`(testIntent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)).thenReturn(testTag)
        `when`(testIntent.getByteArrayExtra(NfcAdapter.EXTRA_ID)).thenReturn(testId)

        val resultNFCTag = nfcService.parseIntent(testIntent)

        Assert.assertEquals(tagDescription, resultNFCTag.tag.toString())
        Assert.assertEquals(ndefMessage.records[0].payload, resultNFCTag.message?.records?.get(0)?.payload)
        Assert.assertEquals(testPayload, resultNFCTag.payload)
        Assert.assertEquals(testId.toHexString(), resultNFCTag.baseTag.id)
    }

    fun createTextRecord(payload: String): ByteArray {
        val langBytes = "en".toByteArray(charset("US-ASCII"))
        val utfEncoding = charset("UTF-8")
        val textBytes = payload.toByteArray(utfEncoding)
        val utfBit = 0
        val status = (utfBit + langBytes.size).toChar()
        val data = ByteArray(1 + langBytes.size + textBytes.size)
        data[0] = status.toByte()
        System.arraycopy(langBytes, 0, data, 1, langBytes.size)
        System.arraycopy(textBytes, 0, data, 1 + langBytes.size, textBytes.size)
        return data
    }
}