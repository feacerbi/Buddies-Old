package br.com.felipeacerbi.buddies.utils

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class PermissionsManager(val activity: Activity) : ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        val REQUEST_CODE = 0
    }

    var actionFunction: (() -> Unit)? = null

    fun actionWithPermission(permission: String, launch: () -> Unit) {
        actionFunction = launch
        if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED) {
            actionFunction?.invoke()
        } else {
            requestPermission(permission)
        }
    }

    private fun requestPermission(permission: String) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_CODE)
//        } else {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
//        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            actionFunction?.invoke()
        }
    }
}