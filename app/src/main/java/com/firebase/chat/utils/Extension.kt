package com.firebase.chat.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat

object Extension {

    private var toast: Toast? = null
    fun Context.toast(message: String) {
        if (toast != null) {
            toast?.cancel()
            toast = null
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast?.show()
    }

    fun Context.checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun Context.checkIsTiramisu(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    fun View.setDelay(delay: Long) {
        this.isClickable = false
        Handler(Looper.getMainLooper()).postDelayed({
            this.isClickable = true
        }, delay)
    }
}