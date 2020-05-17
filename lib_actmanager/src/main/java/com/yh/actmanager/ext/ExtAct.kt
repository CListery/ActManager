package com.yh.actmanager.ext

import android.app.Activity

/**
 * Created by CYH on 2020-03-16 10:08
 */

fun Activity.identifier() = System.identityHashCode(this@identifier).toString()

fun Activity.killSelf() {
    if (Thread.currentThread() == mainLooper.thread) {
        finish()
    } else {
        runOnUiThread {
            finish()
        }
    }
}