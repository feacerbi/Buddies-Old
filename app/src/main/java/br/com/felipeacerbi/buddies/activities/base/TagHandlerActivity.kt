package br.com.felipeacerbi.buddies.activities.base

import android.content.Intent
import android.nfc.NfcAdapter
import android.support.v7.app.AlertDialog
import android.widget.Toast
import br.com.felipeacerbi.buddies.R
import br.com.felipeacerbi.buddies.activities.NewBuddyActivity
import br.com.felipeacerbi.buddies.activities.ProfileActivity
import br.com.felipeacerbi.buddies.activities.QRCodeActivity
import br.com.felipeacerbi.buddies.firebase.FireListener
import br.com.felipeacerbi.buddies.models.BuddyInfo
import br.com.felipeacerbi.buddies.tags.NFCService
import br.com.felipeacerbi.buddies.tags.models.BaseTag
import br.com.felipeacerbi.buddies.utils.PermissionsManager
import br.com.felipeacerbi.buddies.utils.launchActivity
import br.com.felipeacerbi.buddies.utils.launchActivityWithExtras
import br.com.felipeacerbi.buddies.utils.showTwoChoiceCancelableDialog

abstract class TagHandlerActivity : FireListener() {

    companion object {
        var TAG = "TagHandlerActivity"
        val NEW_PET_RESULT = 1000
        val QR_CODE_RESULT = 1001
    }

    val permissionsManager: PermissionsManager by lazy {
        PermissionsManager(this)
    }

    val nfcService: NFCService by lazy {
        NFCService()
    }

    override fun onResume() {
        super.onResume()
        nfcService.activateForegroundDispatchSystem(this)
    }

    override fun onPause() {
        super.onPause()
        nfcService.deactivateForegroundDispatchSystem(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent, false)
    }

    fun handleIntent(intent: Intent?, out: Boolean) {
        if(isNFCIntent(intent)) {
            val nfcTag = nfcService.parseIntent(intent!!)
            val baseTag = nfcTag.baseTag
            if (out) {
                showFullTagOptionsDialog(baseTag)
            } else {
                showTagOptionsDialog(baseTag)
            }
        }
    }

    fun isNFCIntent(intent: Intent?): Boolean {
        return intent != null && intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK) {
            when(requestCode) {
                NEW_PET_RESULT -> {
                    val buddyInfo = data?.extras?.getSerializable(NewBuddyActivity.BUDDY_INFO_EXTRA) as BuddyInfo
                    val tagKey = data.extras.getString(NewBuddyActivity.EXTRA_TAG_KEY)
                    val baseTag = data.extras.getSerializable(NewBuddyActivity.EXTRA_BASETAG) as BaseTag
                    firebaseService.addNewPet(tagKey, baseTag, buddyInfo)
                    launchActivity(ProfileActivity::class)
                }
                QR_CODE_RESULT -> { showTagOptionsDialog(BaseTag(data?.extras?.getString(QRCodeActivity.QR_CODE_TEXT) ?: "")) }
            }
        }
    }

    abstract fun showTagOptionsDialog(baseTag: BaseTag)

    fun showFullTagOptionsDialog(baseTag: BaseTag) {
        AlertDialog.Builder(this).showTwoChoiceCancelableDialog(
                getString(R.string.tag_options_dialog_title),
                getString(R.string.tag_options_dialog_message),
                getString(R.string.tag_options_dialog_follow_button),
                getString(R.string.tag_options_dialog_new_button),
                { _, _ -> addNewFollow(baseTag) },
                { _, _ -> addNewBuddy(baseTag)})
    }

    fun addNewFollow(baseTag: BaseTag) {
        subscriptions.add(subscriptionsManager.checkTagSubscription(
                baseTag,
                usedAction = { firebaseService.addFollowPet(it) },
                newAction = { _, _ -> Toast.makeText(this, "This TAG was not used yet.", Toast.LENGTH_SHORT).show() },
                notVerifiedAction = { Toast.makeText(this, "This TAG was not verified yet.", Toast.LENGTH_SHORT).show() }))
    }

    fun addNewBuddy(baseTag: BaseTag) {
        subscriptions.add(subscriptionsManager.checkTagSubscription(
                baseTag,
                usedAction = {
                    subscriptions.add(subscriptionsManager.checkOwnerRequestSubscription(
                            it,
                            ownsAction = { Toast.makeText(this, getString(R.string.request_toast_already_owns), Toast.LENGTH_SHORT).show() },
                            notOwnsAction = { Toast.makeText(this, getString(R.string.request_toast_sent_message), Toast.LENGTH_SHORT).show() }
                    ))
                },
                newAction = {
                    key, foundTag ->
                    launchActivityWithExtras<NewBuddyActivity>(
                        NewBuddyActivity::class,
                        arrayOf(NewBuddyActivity.EXTRA_TAG_KEY, NewBuddyActivity.EXTRA_BASETAG),
                        arrayOf(key, foundTag),
                        true,
                        NEW_PET_RESULT) },
                notVerifiedAction = { Toast.makeText(this, "This TAG was not verified yet.", Toast.LENGTH_SHORT).show() }))
    }
}
