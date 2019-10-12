package com.hbl.cameraxapp

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

fun Fragment.toast(msg: String) {
    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
}

fun Activity.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Any.log(msg: String) {
    Log.d(javaClass.simpleName, msg)
}