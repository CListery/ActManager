package com.yh.actmanager.ext

import android.app.Activity
import android.graphics.Rect
import com.yh.appinject.logger.LibLogs

/**
 * 检查activity是否有效
 */
val Activity?.isValid get() = !(null == this || isDestroyed || isFinishing)
val Activity?.isInvalid get() = !isValid

val Activity.checkSoftInputVisibility
    get():Boolean {
        val decorView = window?.decorView
        var needHideSoftInput = false
        if (null != decorView) {
            val screenH = decorView.height
            val rect = Rect()
            decorView.getWindowVisibleDisplayFrame(rect)
            LibLogs.logD(
                "checkSoftInputVisibility h: ${screenH * 3 / 4} - b:${rect.bottom}",
                "ExtActivity"
            )
            needHideSoftInput = screenH / 4F * 3 > rect.bottom
        }
        return needHideSoftInput
    }
