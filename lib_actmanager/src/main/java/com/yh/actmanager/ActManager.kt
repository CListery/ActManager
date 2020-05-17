package com.yh.actmanager

import android.app.Activity
import com.yh.actmanager.collections.ActStack
import com.yh.actmanager.cons.ActStatus
import com.yh.appinject.IBaseAppInject
import com.yh.appinject.InjectHelper
import com.yh.appinject.lifecycle.IActStatusEvent
import com.yh.appinject.logger.ext.libW

/**
 * Created by CYH on 2020-03-13 16:35
 */
class ActManager private constructor() : InjectHelper<IBaseAppInject>() {
    
    companion object {
        @JvmStatic
        private val mInstances by lazy { ActManager() }
        
        @JvmStatic
        fun get() = mInstances
    }
    
    private val mActStatusStack = ActStack()
    
    private val mActStatusEvent = object : IActStatusEvent {
        override fun onCreate(target: Activity) {
            mActStatusStack.add(ActInfo(target))
            printAllStatus()
        }
        
        override fun onShow(target: Activity) {
            mActStatusStack.update(target, ActStatus.INSHOW)
            printAllStatus()
        }
        
        override fun onHide(target: Activity) {
            mActStatusStack.update(target, ActStatus.HIDDEN)
            printAllStatus()
        }
        
        override fun onDestroyed(target: Activity) {
            mActStatusStack.update(target, ActStatus.DESTROYED)
            printAllStatus()
        }
    }
    
    override fun init() {
        registerActivityLifecycleCallbacks(mActStatusEvent)
    }
    
    @Synchronized
    fun killAll() {
        unRegisterActivityLifecycleCallbacks(mActStatusEvent)
        mActStatusStack.killAll()
    }
    
    @Synchronized
    fun getLastValidAct(): Activity? {
        return mActStatusStack.findLast { null != it.refAct.get() }?.refAct?.get()
    }
    
    private fun printAllStatus() {
        libW(mActStatusStack.toString())
    }
}