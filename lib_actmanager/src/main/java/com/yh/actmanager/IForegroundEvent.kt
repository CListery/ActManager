package com.yh.actmanager

/**
 * 前/后台状态监听器
 */
interface IForegroundEvent {

    /**
     * 当APP前/后台状态发生变化时将会调用
     * @param isForeground true，APP进入前台
     */
    fun onForegroundStateChange(isForeground: Boolean) {}
}