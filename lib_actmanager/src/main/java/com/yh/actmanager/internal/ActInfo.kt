package com.yh.actmanager.internal

import android.app.Activity
import android.app.isInvalid
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Process
import com.kotlin.memoryId
import java.lang.ref.WeakReference

/**
 * Created by CYH on 2020-03-16 08:58
 */
internal class ActInfo {

    constructor(act: Activity) {
        this.taskID = act.taskId
        this.name = act::class.java.simpleName
        this.pkgName = act.componentName.packageName
        this.identifier = act.memoryId
        this.createTime = System.currentTimeMillis()
        this.updateTime = createTime
        this.isTaskRoot = act.isTaskRoot
        this.activityInfo = null
        this.status = ActStatus.CREATED
        this.refAct = WeakReference(act)
        this.pid = Process.myPid()
    }

    constructor(intent: Intent) {
        this.taskID = intent.getActTaskID()
        this.name = intent.getActName()
        this.pkgName = intent.getActPkgName()
        this.identifier = intent.getActIdentifier()
        this.createTime = intent.getActCreateTime()
        this.updateTime = intent.getActUpdateTime()
        this.isTaskRoot = intent.getActIsTaskRoot()
        this.activityInfo = intent.getActActivityInfo()
        this.status = intent.getActStatus()
        this.refAct = null
        this.pid = intent.getActPid()
    }

    val taskID: Int
    val name: String
    val pkgName: String
    val identifier: String
    val createTime: Long
    var updateTime: Long = -1L

    val isTaskRoot: Boolean

    var activityInfo: ActivityInfo?

    var status: ActStatus

    val refAct: WeakReference<Activity>?

    val pid: Int

    val isValid
        get(): Boolean {
            if (null != refAct) {
                val act = refAct.get()
                if (act.isInvalid) {
                    return false
                }
            }
            return when (status) {
                ActStatus.CREATED,
                ActStatus.STARTED,
                ActStatus.RESUMED,
                ActStatus.PAUSED,
                ActStatus.STOPPED -> true
                ActStatus.DESTROYED -> false
            }
        }

    fun kill(disableAnim: Boolean = false) {
        if (isValid) {
            refAct?.get()?.finish()
            if (disableAnim) {
                refAct?.get()?.overridePendingTransition(0, 0)
            }
        }
    }

    fun toIntent(intent: Intent, act: Activity? = null) {
        intent.putExtra("task", taskID)
        intent.putExtra("name", name)
        intent.putExtra("pkg_name", pkgName)
        intent.putExtra("identifier", identifier)
        intent.putExtra("create_time", createTime)
        intent.putExtra("update_time", updateTime)
        intent.putExtra("is_task_root", isTaskRoot)
        intent.putExtra("activity_info", activityInfo)
        intent.putExtra("status", status)
        intent.putExtra("pid", pid)
        intent.putExtra("isChangingConfigurations", act?.isChangingConfigurations ?: false)
    }

}

internal fun Intent.getActTaskID() = getIntExtra("task", -1)
internal fun Intent.getActName() = getStringExtra("name") ?: ""
internal fun Intent.getActPkgName() = getStringExtra("pkg_name") ?: ""
internal fun Intent.getActIdentifier() = getStringExtra("identifier") ?: ""
internal fun Intent.getActCreateTime() = getLongExtra("create_time", -1)
internal fun Intent.getActUpdateTime() = getLongExtra("update_time", -1)
internal fun Intent.getActIsTaskRoot() = getBooleanExtra("is_task_root", false)
internal fun Intent.getActActivityInfo() = getParcelableExtra<ActivityInfo>("activity_info")
internal fun Intent.getActStatus() =
    getSerializableExtra("status") as? ActStatus ?: ActStatus.CREATED

internal fun Intent.getActPid() = getIntExtra("pid", -1)

internal fun Intent.getActivityIsChangingConfigurations() =
    getBooleanExtra("isChangingConfigurations", false)
