package com.yh.actmanager

import android.app.Activity
import android.text.TextUtils
import com.yh.actmanager.cons.ActStatus
import com.yh.actmanager.ext.identifier
import com.yh.appinject.IBaseAppInject
import com.yh.appinject.InjectHelper
import com.yh.appinject.ext.LogW
import com.yh.appinject.lifecycle.IActStatusEvent

/**
 * Created by CYH on 2020-03-13 16:35
 */
class ActManager private constructor() : InjectHelper<IBaseAppInject>() {
    
    companion object {
        private const val TAG = "ActManager"
        
        @JvmStatic
        private val mInstances by lazy { ActManager() }
        
        @JvmStatic
        fun get() = mInstances
    }
    
    private val mActStatusStack = LinkedHashSet<ActInfo>()
    
    private val mActStatusEvent = object : IActStatusEvent {
        override fun onCreate(target: Activity) {
            autoRemoveInvalidAct()
            val actInfo = makeAndSaveAct(target)
            if (!actInfo.isTaskRootAct) {
                val parentInfo = findTaskLastAct(actInfo)
                if (null != parentInfo) {
                    parentInfo.child = actInfo.identifier
                    actInfo.parent = parentInfo.identifier
                }
            }
            printAllStatus()
        }
        
        override fun onShow(target: Activity) {
            autoRemoveInvalidAct()
            val stackTarget = findStackTargetAct(target)
            if (null != stackTarget) {
                stackTarget.actStatus = ActStatus.INSHOW
            }
            printAllStatus()
        }
        
        override fun onHide(target: Activity) {
            autoRemoveInvalidAct()
            val stackTarget = findStackTargetAct(target)
            if (null != stackTarget) {
                stackTarget.actStatus = ActStatus.HIDDEN
            }
            printAllStatus()
        }
        
        override fun onDestroyed(target: Activity) {
            val stackTarget = findStackTargetAct(target)
            if (null != stackTarget) {
                stackTarget.actStatus = ActStatus.DESTROYED
            }
            printAllStatus()
            autoRemoveInvalidAct()
        }
    }
    
    @Synchronized
    private fun findTaskLastAct(actInfo: ActInfo): ActInfo? {
        return mActStatusStack.filter { it.taskID == actInfo.taskID }.filterNot { it.identifier == actInfo.identifier }.maxBy { it.createTime }
    }
    
    @Synchronized
    private fun makeAndSaveAct(target: Activity): ActInfo {
        val actInfo = ActInfo(target)
        mActStatusStack.add(actInfo)
        return actInfo
    }
    
    @Synchronized
    private fun findStackTargetAct(target: Activity): ActInfo? {
        return mActStatusStack.find {
            val identity = it.identifier.split(":").getOrNull(1)
            if (TextUtils.isEmpty(identity)) {
                it.refAct.get() == target
            } else {
                identity == target.identifier()
            }
        }
    }
    
    @Synchronized
    private fun autoRemoveInvalidAct() {
        val emptyStack = mActStatusStack.filter { null == it.refAct.get() || ActStatus.DESTROYED == it.actStatus }
        if (emptyStack.isNotEmpty()) {
            mActStatusStack.removeAll(emptyStack)
        }
        emptyStack.forEach { empty ->
            mActStatusStack.find { it.parent == empty.identifier }?.let { info ->
                info.parent = null
            }
            mActStatusStack.find { it.child == empty.identifier }?.let { info ->
                info.child = null
            }
        }
    }
    
    override fun init() {
        registerActivityLifecycleCallbacks(mActStatusEvent)
    }
    
    @Synchronized
    private fun printAllStatus() {
        val sb = StringBuilder()
        sb.append("-----------[START] ACT STATUS-----------\n")
        mActStatusStack.forEach {
            sb.append(">>> $it\n")
        }
        sb.append(">== ${mActStatusStack.filter { ActStatus.INSHOW == it.actStatus }}\n")
        sb.append("-----------[ END ] ACT STATUS-----------")
        LogW(TAG, sb.toString())
    }
    
}