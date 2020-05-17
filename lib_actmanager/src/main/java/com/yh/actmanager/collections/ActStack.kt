package com.yh.actmanager.collections

import android.app.Activity
import android.text.TextUtils
import com.yh.actmanager.ActInfo
import com.yh.actmanager.cons.ActStatus
import com.yh.actmanager.ext.identifier
import com.yh.actmanager.ext.killSelf

/**
 * Created by CYH on 2020/5/12 13:53
 */
class ActStack : AbstractMutableCollection<ActInfo>() {
    
    private val mActStatusStack = LinkedHashSet<ActInfo>()
    
    fun killAll() {
        cleanup()
        val it = iterator()
        while (it.hasNext()) {
            val actInfo = it.next()
            actInfo.refAct.get()?.killSelf()
            actInfo.refAct.clear()
            it.remove()
        }
    }
    
    fun update(target: Activity, status: ActStatus): Boolean {
        cleanup()
        val stackTarget = findTarget(target)
        if (null != stackTarget && stackTarget.actStatus != status) {
            stackTarget.actStatus = status
            return true
        }
        return false
    }
    
    override fun add(element: ActInfo): Boolean {
        cleanup()
        val result = mActStatusStack.add(element)
        if (result) {
            if (!element.isTaskRootAct) {
                val parentInfo = findLast(element)
                if (null != parentInfo) {
                    parentInfo.child = element.identifier
                    element.parent = parentInfo.identifier
                }
            }
        }
        return result
    }
    
    private fun cleanup() {
        val emptyStack =
                mActStatusStack.filter { null == it.refAct.get() || ActStatus.DESTROYED == it.actStatus }
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
    
    private fun findLast(actInfo: ActInfo): ActInfo? {
        return mActStatusStack.filter { it.taskID == actInfo.taskID }
                .filterNot { it.identifier == actInfo.identifier }.maxBy { it.createTime }
    }
    
    private fun findTarget(target: Activity): ActInfo? {
        return mActStatusStack.find {
            if (TextUtils.isEmpty(it.identifier)) {
                it.refAct.get() == target
            } else {
                it.identifier == target.identifier()
            }
        }
    }
    
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("-----------[START] ACT STATUS-----------\n")
        mActStatusStack.forEach {
            sb.append(">>> $it\n")
        }
        sb.append(">== ${mActStatusStack.filter { ActStatus.INSHOW == it.actStatus }}\n")
        sb.append("-----------[ END ] ACT STATUS-----------")
        return sb.toString()
    }
    
    override fun contains(element: ActInfo): Boolean {
        val act = element.refAct.get()
        if (null == act || !element.isValid()) {
            return false
        }
        return null != findTarget(act)
    }
    
    override fun iterator(): MutableIterator<ActInfo> {
        cleanup()
        return ActStackIterator(mActStatusStack.iterator())
    }
    
    override val size: Int
        get() {
            cleanup()
            return mActStatusStack.size
        }
    
    private inner class ActStackIterator(private val iterator: MutableIterator<ActInfo>) :
            MutableIterator<ActInfo> {
        
        private var next: ActInfo? = null
        
        override fun hasNext(): Boolean {
            if (null != next) {
                return true
            }
            while (iterator.hasNext()) {
                val actInfo = iterator.next()
                if (actInfo.isValid()) {
                    //to ensure next() can't throw after hasNext() returned true, we need to dereference this
                    next = actInfo
                    return true
                }
            }
            return false
        }
        
        override fun next(): ActInfo {
            var result = next
            next = null
            while (null == result) {
                result = iterator.next()
            }
            return result
        }
        
        override fun remove() {
            iterator.remove()
        }
    }
    
}