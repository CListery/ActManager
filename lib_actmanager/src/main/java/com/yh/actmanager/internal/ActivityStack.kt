package com.yh.actmanager.internal

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import com.kotlin.memoryId
import com.yh.actmanager.ActManager
import com.yh.appbasic.logger.logD

internal class ActivityStack {

    private val actInfoList = ArrayList<ActInfo>()
    private val activityInfoList = arrayListOf<ActivityInfo>()

    val topAct get() = actInfoList.lastOrNull { it.isValid && it.refAct?.get() != null }?.refAct?.get()
    val topActInfo get() = actInfoList.lastOrNull { it.isValid }

    fun updateByIntent(intent: Intent): ActInfo? {
        return when (val status = intent.getActStatus()) {
            ActStatus.CREATED -> {
                val intentActInfo = ActInfo(intent)
                actInfoList.add(intentActInfo)
                return intentActInfo
            }
            ActStatus.STARTED,
            ActStatus.RESUMED,
            ActStatus.PAUSED,
            ActStatus.STOPPED,
            ActStatus.DESTROYED -> {
                findTargetByIntent(intent)?.apply {
                    this.status = status
                }
            }
        }
    }

    fun update(act: Activity, status: ActStatus): ActInfo? {
        return when (status) {
            ActStatus.CREATED -> {
                val actInfo = ActInfo(act)
                actInfo.activityInfo = findActivityInfo(actInfo)
                actInfoList.add(actInfo)
                return actInfo
            }
            ActStatus.STARTED,
            ActStatus.RESUMED,
            ActStatus.PAUSED,
            ActStatus.STOPPED,
            ActStatus.DESTROYED -> {
                findTargetByActivity(act)?.apply {
                    this.status = status
                }
            }
        }
    }

    fun cleanup() {
        actInfoList.removeAll {
            if (null != it.refAct) {
                val act = it.refAct.get()
                if (null == act) {
                    return@removeAll true
                } else {
                    return@removeAll act.isDestroyed || it.status == ActStatus.DESTROYED
                }
            }
            it.status == ActStatus.DESTROYED
        }
        logD("cleanup: ${actInfoList.map { "[${it.status}]${it.tag}" }}", loggable = ActManager.get())
    }

    fun findTargetByIntent(intent: Intent): ActInfo? {
        return all().find { it.identifier == intent.getActIdentifier() }
    }

    fun findTargetByActivity(target: Activity): ActInfo? {
        return all().find { it.identifier == target.memoryId }
    }

    fun all() = actInfoList.toList()

    fun killAll() {
        all().reversed().forEach {
            it.kill(true)
        }
        actInfoList.clear()
    }

    fun updateActivityInfo(list: List<ActivityInfo>) {
        activityInfoList.clear()
        activityInfoList.addAll(list)
        val all = all()
        if (all.isNotEmpty()) {
            all.forEach { act ->
                act.activityInfo = findActivityInfo(act)
            }
        }
    }

    private fun findActivityInfo(act: ActInfo): ActivityInfo? {
        val copy = activityInfoList.toList()
        if (copy.isEmpty()) {
            return null
        }
        return copy.find { act.name == it.name && act.pkgName == it.packageName }
    }

}

internal inline val Activity?.checkInvalid get() = null == this || isDestroyed
internal inline val Activity.tag get() = "${this::class.java.simpleName}[${memoryId}] TID:${taskId}"
internal inline val Intent.tag get() = "${getActName()}[${getActIdentifier()}] TID:${getActTaskID()}"
internal inline val ActInfo.tag get() = "${name}[${identifier}] TID:${taskID}"
