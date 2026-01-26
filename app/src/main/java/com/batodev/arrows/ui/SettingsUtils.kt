package com.batodev.arrows.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory

object SettingsUtils {
    fun launchBrowser(context: Context, url: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchEmail(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = packageInfo.versionName
            val device = "${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})"
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@emberfox.online"))
                putExtra(Intent.EXTRA_SUBJECT, "Arrows Game Support")
                putExtra(Intent.EXTRA_TEXT, "\n\n\n---\nApp Version: $version\nDevice: $device")
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchReviewFlow(context: Context) {
        if (context !is Activity) return
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                manager.launchReviewFlow(context, task.result)
            } else {
                Toast.makeText(context, "Could not launch review flow", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
