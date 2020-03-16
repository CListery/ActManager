package com.yh.actmanager

import android.app.Activity
import com.yh.actmanager.cons.ActStatus
import com.yh.actmanager.ext.identifier
import java.lang.ref.SoftReference
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
    val refAct: SoftReference<Activity> = SoftReference(act)
    
    override fun toString(): String {
        return "S:[${actStatus}] - T:[${taskID}] - I:[${identifier}] - N:[$name] - C:[${formatDate(createTime)}] - R:[${isTaskRootAct}] - <${parent} - ${child}>"
    }
    
    private fun formatDate(time: Long?, locale: Locale = Locale.ENGLISH): String {
        if (null != time && time > 0) {
            return SimpleDateFormat("H:mm:ss", locale).format(time)
        }
        return "null"
    }
}