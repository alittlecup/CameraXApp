package com.hbl.cameraxapp.utils

import android.os.Build
import android.view.DisplayCutout
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog


const val FLAGS_FULLSCREEN =
    View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_LOW_MILLIS = 100L

fun ImageButton.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick()
    isPressed = true
    invalidate()
    postDelayed({
        invalidate()
        isPressed = false
    }, delay)
}

@RequiresApi(Build.VERSION_CODES.P)
fun View.padWithDisplayCutout() {
    fun doPadding(cutout: DisplayCutout) = setPadding(
        cutout.safeInsetLeft,
        cutout.safeInsetTop,
        cutout.safeInsetRight,
        cutout.safeInsetBottom
    )
    rootWindowInsets?.displayCutout?.let {
        doPadding(it)
    }
    setOnApplyWindowInsetsListener { view, insets ->
        insets.displayCutout?.let { doPadding(it) }
        insets
    }
}

fun AlertDialog.showImm() {
    window?.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    )
    window?.decorView?.systemUiVisibility = FLAGS_FULLSCREEN
    show()
    window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
}
