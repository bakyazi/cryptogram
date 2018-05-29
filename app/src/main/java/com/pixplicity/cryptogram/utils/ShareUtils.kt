package com.pixplicity.cryptogram.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.fragments.AboutFragment
import com.pixplicity.cryptogram.views.SimpleInputConnection

fun getVersionString(context: Context): String? {
    var versionString: String? = null
    try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        versionString = context.getString(R.string.version, info.versionName, info.versionCode)
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("ShareUtils", "Package not found", e)
    }
    return versionString
}

fun sendFeedback(context: Context, orderId: String?) {
    var versionString: String? = getVersionString(context)

    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto", AboutFragment.FEEDBACK_EMAIL, null))
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(AboutFragment.FEEDBACK_EMAIL))
    intent.putExtra(Intent.EXTRA_SUBJECT,
            if (orderId != null) context.getString(R.string.feedback_subject_donation, orderId)
            else context.getString(R.string.feedback_subject))
    val ime = SimpleInputConnection.getIme(context)
    val keyboardPackageName = if (ime == null) "unknown" else ime.packageName
    val message = context.getString(if (orderId != null) R.string.feedback_body_donation else R.string.feedback_body,
            versionString,
            keyboardPackageName,
            Build.VERSION.RELEASE,
            Build.MANUFACTURER + " " + Build.MODEL)
    intent.putExtra(Intent.EXTRA_TEXT, message)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, R.string.error_no_activity, Toast.LENGTH_LONG).show()
    }
}

fun donationThankYou(context: Context, orderId: String) {
    AlertDialog.Builder(context)
            .setMessage(R.string.donate_thank_you)
            .setPositiveButton(R.string.donate_thank_you_feedback, { dialog, _ ->
                sendFeedback(context, orderId)
                dialog.dismiss()
            })
            .setNegativeButton(R.string.donate_thank_you_continue, { dialog, _ ->
                dialog.dismiss()
            })
            .show()
}
