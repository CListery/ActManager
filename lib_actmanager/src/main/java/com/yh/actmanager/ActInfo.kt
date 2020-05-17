package com.yh.actmanager

import android.app.Activity
import com.yh.actmanager.cons.ActStatus
import com.yh.actmanager.ext.identifier
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CYH on 2020-03-16 08:58
 */
class ActInfo(act: Activity) {
    val taskID = act.taskId
    val name = act::class.java.simpleName
    val pkgName = act.componentName.packageName
    val identifier = act.identifier()
    val createTime = System.currentTimeMillis()
    
    val isTaskRootAct = act.isTaskRoot
    
    var parent: String? = null
    var child: String? = null
    
    var actStatus: ActStatus = ActStatus.CREATED
    val refAct: WeakReference<Activity> = WeakReference(act)
    
    override fun toString(): String {
        return "S:[${actStatus}] - T:[${taskID}] - I:[${identifier}] - N:[$name] - C:[${formatDate(
                createTime
        )}] - R:[${isTaskRootAct}] - <${parent} - ${child}>"
    }
    
    private fun formatDate(time: Long?, locale: Locale = Locale.ENGLISH): String {
        if (null != time && time > 0) {
            return SimpleDateFormat("H:mm:ss", locale).format(time)
        }
        return "null"
    }
    
    fun isValid(): Boolean {
        return null != refAct.get() && ActStatus.DESTROYED != actStatus
    }
    
    override fun equals(other: Any?): Boolean {
        if (other is ActInfo) {
            return other.taskID == taskID
                    && other.identifier == identifier
                    && other.createTime == createTime
                    && other.actStatus == actStatus
                    && other.refAct.get() == refAct.get()
        }
        return false
    }
    
    override fun hashCode(): Int {
        var result = taskID
        result = 31 * result + identifier.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + actStatus.hashCode()
        result = 31 * result + refAct.get().hashCode()
        return result
    }
}