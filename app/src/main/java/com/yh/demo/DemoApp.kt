package com.yh.demo

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.yh.actmanager.ActManager
import com.yh.actmanager.IForegroundEvent
import com.yh.appbasic.logger.logOwner
import com.yh.appbasic.share.AppBasicShare
import com.yh.appinject.IBaseAppInject

/**
 * Created by CYH on 2020-03-16 15:54
 */
class DemoApp : Application(), IBaseAppInject, IForegroundEvent {
    private var mCtx: Application? = null

    override fun getNotificationIcon() = R.mipmap.ic_launcher

    override fun showTipMsg(msg: String) {
        Toast.makeText(mCtx, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        super.onCreate()

        AppBasicShare.install(this)

        mCtx = this

        ActManager.get().apply {
            logOwner.on()
            register(this@DemoApp)
            registerEvent(this@DemoApp)
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