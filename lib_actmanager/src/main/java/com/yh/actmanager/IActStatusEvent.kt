package com.yh.actmanager

import android.app.Activity

/**
 * Activity状态监听器
 */
interface IActStatusEvent : IForegroundEvent {

    /**
     * @see Activity.onCreate
     */
    fun onCreate(target: Activity)

    /**
     * @see Activity.onResume
     */
    fun onShow(target: Activity)

    /**
     * @see Activity.onPause
     */
    fun onHide(target: Activity)

    /**
     * @see Activity.onDestroy
     */
    fun onDestroyed(target: Activity)
}