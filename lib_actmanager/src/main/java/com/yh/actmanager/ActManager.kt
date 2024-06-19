package com.yh.actmanager

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.getSystemService
import com.kotlin.memoryId
import com.yh.actmanager.internal.*
import com.yh.appbasic.share.AppBasicShare
import com.yh.appbasic.logger.*
import com.yh.appinject.IBaseAppInject
import com.yh.appinject.InjectHelper
import kotlin.concurrent.thread

/**
 * Created by CYH on 2020-03-13 16:35
 */
class ActManager private constructor() : InjectHelper<IBaseAppInject>(), ILogger {

    companion object {
        @JvmStatic
        private val mInstances by lazy { ActManager() }

        @JvmStatic
        fun get() = mInstances

        /**********************************PERMISSIONS********************************************/
        private val BROADCAST_PERMISSIONS by lazy { "${AppBasicShare.context.packageName}.permissions.act_process" }

        /**********************************ACTION*************************************************/
        // 关闭所有 activity
        private const val ACTION_KILL_ALL_ACT = "action_kill_all_act"

        // 更新 ActInfo
        private const val ACTION_UPDATE_ACT_INFO = "action_update_act_info"

        // 强制返回
        private const val ACTION_FINISH_FORCE_GO_BACK = "action_finish_force_go_back"

        // 关闭当前 activity 并尝试恢复前一个栈
        private const val ACTION_FINISH_AND_OPEN_PRE_TASK = "action_finish_and_open_pre_task"

        // 关闭指定 activity
        private const val ACTION_FINISH_TARGET_ACT = "action_finish_target_act"

        /**********************************CATEGORIES*********************************************/
        private const val CATEGORY_MAIN_PROCESS = "category_main_process"
        private const val CATEGORY_OTHER_PROCESS = "category_other_process"
    }

    private val sysActMgr: ActivityManager? by lazy { AppBasicShare.context.getSystemService() }
    private val sysPkgMgr: PackageManager? by lazy { AppBasicShare.context.packageManager }

    private var isInitialized = false

    private val activityStack = ActivityStack()

    private var isForcedStackTopMode = false
    private var mLastTaskId: Int = -1
    private var isMainProcess: Boolean = false

    private val events = arrayListOf<IForegroundEvent>()

    override fun onCreateLogOwner(logOwner: LogOwner) {
        
    }

    override fun init() {
        if (isInitialized) {
            return
        }
        isInitialized = true
        isMainProcess = AppBasicShare.context.isMainProcess()
        if (isMainProcess) {
            registerProcessReceiver(
                listOf(
                    ACTION_KILL_ALL_ACT,
                    ACTION_UPDATE_ACT_INFO,
                    ACTION_FINISH_FORCE_GO_BACK,
                    ACTION_FINISH_AND_OPEN_PRE_TASK,
                ),
                listOf(CATEGORY_MAIN_PROCESS)
            )
        } else {
            registerProcessReceiver(
                listOf(
                    ACTION_KILL_ALL_ACT,
                    ACTION_FINISH_FORCE_GO_BACK,
                    ACTION_FINISH_AND_OPEN_PRE_TASK,
                    ACTION_FINISH_TARGET_ACT,
                ),
                listOf(
                    CATEGORY_OTHER_PROCESS,
                    getProcessCategory(AppBasicShare.pid)
                )
            )
        }
        loadActivityInfo()
        AppBasicShare.application.registerActivityLifecycleCallbacks(callback)
    }

    private fun getProcessCategory(pid: Int) = CATEGORY_OTHER_PROCESS.plus(":$pid")

    private fun registerProcessReceiver(actions: List<String>, categories: List<String>) {
        val intentFilter = IntentFilter()
        actions.forEach {
            intentFilter.addAction(it)
        }
        categories.forEach {
            intentFilter.addCategory(it)
        }
        AppBasicShare.context.registerReceiver(mActivityProcessReceiver, intentFilter, BROADCAST_PERMISSIONS, null)
    }

    private fun loadActivityInfo() {
        thread {
           logD("loadActivityInfo: ${AppBasicShare.context.packageName} $sysPkgMgr", this)
            val packageInfo =
                sysPkgMgr?.getPackageInfo(AppBasicShare.context.packageName, PackageManager.GET_ACTIVITIES)
            if (null == packageInfo) {
               logE("loadActivityInfo FAIL!", loggable = this)
                return@thread
            }
            val list = packageInfo.activities.filterNotNull()
           logD("loadActivityInfo: ${list.size}")
            activityStack.updateActivityInfo(list)
        }
    }

    /**
     * APP是否处于前台
     * @return true，当前处于前台
     */
    fun isForeground() = mInStackActCount > 0

    /**
     * 注册监听器
     *
     * @see [com.yh.actmanager.IActStatusEvent]
     * @see [com.yh.actmanager.IForegroundEvent]
     */
    fun registerEvent(event: IForegroundEvent) {
        if (events.contains(event)) {
            return
        }
        events.add(event)
    }

    /**
     * 注销监听器
     *
     * @see [com.yh.actmanager.IActStatusEvent]
     * @see [com.yh.actmanager.IForegroundEvent]
     */
    fun unregisterEvent(event: IForegroundEvent) {
        events.remove(event)
    }

    /**
     * 是否开启强制栈顶模式
     *
     * 当 APP 进入后台后会自动记录栈顶 activity，在重新打开 APP 时会自动恢复到栈顶 activity
     *
     * 使用情形：
     * * 当前栈打开顺序为 A(normal) -> B(singleInstance)
     *
     * 当进入后台时栈顶为 B，默认情况下，重新打开 APP 时会回到 A，
     * 开启该功能后，会回到 B
     */
    fun enableForcedStackTopMode(flag: Boolean) {
        if (!isMainProcess) {
            return
        }
       logD("enableForcedStackTopMode: $flag - $isForcedStackTopMode", this)
        if (flag == isForcedStackTopMode) {
            return
        }
        isForcedStackTopMode = flag
    }

    /**
     * 关闭所有 activity
     */
    fun killAll() {
        val intent = Intent(ACTION_KILL_ALL_ACT)
        if (isMainProcess) {
            intent.addCategory(CATEGORY_OTHER_PROCESS)
        } else {
            intent.addCategory(CATEGORY_MAIN_PROCESS)
        }
        send(intent)
        killAllByProcess()
    }

    /**
     * 关闭当前进程所有 activity
     */
    fun killAllByProcess() {
        activityStack.killAll()
    }

    /**
     * 关闭指定进程的所有 activity
     */
    fun killByProcess(pid: Int) {
        val intent = Intent(ACTION_KILL_ALL_ACT)
        intent.addCategory(getProcessCategory(pid))
        send(intent)
    }

    /**
     * 获取当前进程的栈顶 activity
     */
    val topAct get() = activityStack.topAct

    /**
     * 强制按打开顺序返回
     */
    fun finishForceGoBack(act: Activity) {
        val targetActInfo = activityStack.findTargetByActivity(act)
        if (null == targetActInfo) {
           logW("finishForceGoBack: not found this act in ActStack!", loggable = this)
            act.finish()
            return
        }
        if (!isMainProcess) {
            val intent = Intent(ACTION_FINISH_FORCE_GO_BACK)
            intent.addCategory(CATEGORY_MAIN_PROCESS)
            targetActInfo.toIntent(intent)
            send(intent)
            return
        }
        internalFinishForceGoBack(targetActInfo)
        act.finish()
    }

    private fun finishForceGoBackFromProcess(intent: Intent) {
        val processActInfo = ActInfo(intent)
        internalFinishForceGoBack(processActInfo)

        val closeIntent = Intent(ACTION_FINISH_TARGET_ACT)
        closeIntent.addCategory(getProcessCategory(processActInfo.pid))
        processActInfo.toIntent(closeIntent)
        send(closeIntent)
    }

    private fun internalFinishForceGoBack(targetActInfo: ActInfo) {
        val allActs = activityStack.all()
        if (allActs.isNullOrEmpty()) {
           logW("finishForceGoBack: ActStack is empty!", this)
            return
        }
        val targetIndex = allActs.indexOfFirst { it.identifier == targetActInfo.identifier }
        if (targetIndex == -1) {
           logW("finishForceGoBack: not found index for this act!", this)
            return
        }
        if (targetIndex == 0) {
            return
        }
        val preActInfo = allActs.getOrNull(targetIndex - 1)
        if (null == preActInfo) {
           logW("finishForceGoBack: not found pre act!", this)
            return
        }
       logD("finishForceGoBack: ${targetActInfo.tag} -> ${preActInfo.tag}", this)
        if (preActInfo.taskID != targetActInfo.taskID) {
            sysActMgr?.moveTaskToFront(preActInfo.taskID, ActivityManager.MOVE_TASK_WITH_HOME)
        }
    }

    /**
     * 关闭当前 activity 并尝试恢复上一个堆栈到前台
     */
    fun finishAndOpenPreTask(act: Activity, flag: Int = ActivityManager.MOVE_TASK_WITH_HOME) {
        val targetActInfo = activityStack.findTargetByActivity(act)
        if (null == targetActInfo) {
           logW("finishAndOpenPreTask: not found this act in ActStack!", this)
            act.finish()
            return
        }
        if (!isMainProcess) {
            val intent = Intent(ACTION_FINISH_AND_OPEN_PRE_TASK)
            intent.addCategory(CATEGORY_MAIN_PROCESS)
            targetActInfo.toIntent(intent)
            intent.putExtra("flag", flag)
            send(intent)
            return
        }
        internalFinishAndOpenPreTask(targetActInfo, flag)
        act.finish()
    }

    private fun finishAndOpenPreTaskFromProcess(intent: Intent) {
        val processActInfo = ActInfo(intent)
        internalFinishAndOpenPreTask(
            processActInfo,
            intent.getIntExtra("flag", ActivityManager.MOVE_TASK_WITH_HOME)
        )

        val closeIntent = Intent(ACTION_FINISH_TARGET_ACT)
        closeIntent.addCategory(getProcessCategory(processActInfo.pid))
        processActInfo.toIntent(closeIntent)
        send(closeIntent)
    }

    private fun internalFinishAndOpenPreTask(targetActInfo: ActInfo, flag: Int) {
        val allActs = activityStack.all()
        if (allActs.isNullOrEmpty()) {
           logW("finishAndOpenPreTask: ActStack is empty!", this)
            return
        }
        val targetIndex = allActs.indexOfFirst { it.identifier == targetActInfo.identifier }
        if (targetIndex == -1) {
           logW("finishAndOpenPreTask: not found index for this act!", this)
            return
        }
        if (targetIndex == 0) {
            return
        }
        val preTaskID =
            allActs.subList(0, targetIndex).findLast { it.taskID != targetActInfo.taskID }?.taskID
        if (null == preTaskID) {
           logW("finishAndOpenPreTask: not found pre task id!", this)
            return
        }
       logD("finishAndOpenPreTask: found pre task id $preTaskID", this)
        sysActMgr?.moveTaskToFront(preTaskID, flag)
    }

    private val mActivityProcessReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
               logD("onReceive[${intent?.action}]: ${context.memoryId} - ${intent?.categories}", this)
                if (null == intent) {
                    return
                }
                if (intent.hasCategory(CATEGORY_MAIN_PROCESS)) {
                    when (intent.action) {
                        ACTION_KILL_ALL_ACT -> {
                            killAllByProcess()
                        }
                        ACTION_UPDATE_ACT_INFO -> {
                            updateFromProcess(intent)
                        }
                        ACTION_FINISH_FORCE_GO_BACK -> {
                            finishForceGoBackFromProcess(intent)
                        }
                        ACTION_FINISH_AND_OPEN_PRE_TASK -> {
                            finishAndOpenPreTaskFromProcess(intent)
                        }
                    }
                } else if (intent.hasCategory(CATEGORY_OTHER_PROCESS)
                    || intent.hasCategory(getProcessCategory(AppBasicShare.pid))
                ) {
                    when (intent.action) {
                        ACTION_KILL_ALL_ACT -> {
                            killAllByProcess()
                        }
                        ACTION_FINISH_TARGET_ACT -> {
                            val targetActInfo = activityStack.findTargetByIntent(intent)
                           logD("onReceive[${intent.action}]: ${targetActInfo?.tag}", this)
                            targetActInfo?.kill()
                        }
                    }
                }
            }
        }
    }

    private fun updateFromProcess(intent: Intent) {
        val pid = intent.getActPid()
        if (pid == -1) {
           logW("updateFromProcess: pid invalid!", this)
            return
        }
        val actInfo = activityStack.updateByIntent(intent)
       logD("updateFromProcess[${intent.getActStatus()}]: ${intent.tag} $pid $actInfo", this)
        checkForeground(intent.getActStatus(), intent.getActivityIsChangingConfigurations())
        activityStack.cleanup()
    }

    private fun updateAct(act: Activity, status: ActStatus) {
        val actInfo = activityStack.update(act, status)
       logD("updateAct[$status]: ${act.tag} $actInfo", this)
        if (!isMainProcess && null != actInfo) {
            val intent = Intent(ACTION_UPDATE_ACT_INFO)
            intent.addCategory(CATEGORY_MAIN_PROCESS)
            actInfo.toIntent(intent, act)
            send(intent)
        } else {
            checkForeground(status, act.isChangingConfigurations)
        }
        activityStack.cleanup()
    }

    private fun send(intent: Intent) {
       logD("send: $isMainProcess ${AppBasicShare.pid} ${intent.action} ${intent.categories}", this)
        AppBasicShare.context.sendBroadcast(intent, BROADCAST_PERMISSIONS)
    }

    private val callback = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            updateAct(activity, ActStatus.CREATED)
            events.filterIsInstance<IActStatusEvent>().forEach {
                it.onCreate(activity)
            }
        }

        override fun onActivityStarted(activity: Activity) {
            updateAct(activity, ActStatus.STARTED)
        }

        override fun onActivityResumed(activity: Activity) {
            updateAct(activity, ActStatus.RESUMED)
            events.filterIsInstance<IActStatusEvent>().forEach {
                it.onShow(activity)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            updateAct(activity, ActStatus.PAUSED)
            events.filterIsInstance<IActStatusEvent>().forEach {
                it.onHide(activity)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            updateAct(activity, ActStatus.STOPPED)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            updateAct(activity, ActStatus.DESTROYED)
            events.filterIsInstance<IActStatusEvent>().forEach {
                it.onDestroyed(activity)
            }
        }
    }

    private var mIsChangingConfiguration = false
    private var mInStackActCount = 0
    private fun checkForeground(status: ActStatus, isChangingConfigurations: Boolean) {
        if (!isMainProcess) {
            return
        }
        when (status) {
            ActStatus.STARTED -> {
                if (mIsChangingConfiguration) {
                    mIsChangingConfiguration = false
                    return
                }
                if (++mInStackActCount == 1) {
                    val topActInfo = activityStack.topActInfo
                   logD("enter FG: ${topActInfo?.tag} $isForcedStackTopMode $mLastTaskId", this)
                    thread {
                        events.toList().forEach {
                            it.onForegroundStateChange(true)
                        }
                    }
                    if (isForcedStackTopMode) {
                        if (mLastTaskId != -1) {
                            sysActMgr?.moveTaskToFront(
                                mLastTaskId,
                                ActivityManager.MOVE_TASK_NO_USER_ACTION
                            )
                            mLastTaskId = -1
                        }
                    }
                }
            }
            ActStatus.STOPPED -> {
                if (isChangingConfigurations) {
                    mIsChangingConfiguration = isChangingConfigurations
                    return
                }
                if (--mInStackActCount == 0) {
                    val topActInfo = activityStack.topActInfo
                   logD("enter BG: ${topActInfo?.tag} $isForcedStackTopMode $mLastTaskId", this)
                    thread {
                        events.toList().forEach {
                            it.onForegroundStateChange(false)
                        }
                    }
                    if (null == topActInfo) {
                        // 按下返回键退出
                        mLastTaskId = -1
                    } else {
                        // 按下 home 键退出
                        if (isForcedStackTopMode) {
                            mLastTaskId = topActInfo.taskID
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

}