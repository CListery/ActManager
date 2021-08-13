package com.yh.demo

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.yh.actmanager.ActManager
import com.yh.actmanager.IForegroundEvent
import com.yh.appinject.IBaseAppInject
import com.yh.appinject.ext.isMainProcess

/**
 * Created by CYH on 2020-03-16 15:54
 */
class DemoApp : Application(), IBaseAppInject, IForegroundEvent {
    private var mCtx: Application? = null

    override fun getApplication() = this

    override fun getNotificationIcon() = R.mipmap.ic_launcher

    override fun showTipMsg(msg: String) {
        Toast.makeText(mCtx, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        super.onCreate()

        mCtx = getApplication()

        ActManager.get().apply {
            loggerConfig(true to Log.VERBOSE)
            register(this@DemoApp)
            registerForegroundEvent(this@DemoApp)
        }
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }

    override fun onForegroundStateChange(isForeground: Boolean) {
        Handler(Looper.getMainLooper()).post { showTipMsg(if(isForeground) "进入前台" else "进入后台") }
    }

}