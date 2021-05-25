package com.yh.demo

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.yh.actmanager.ActManager
import com.yh.appinject.IBaseAppInject
import com.yh.appinject.ext.isMainProcess

/**
 * Created by CYH on 2020-03-16 15:54
 */
class DemoApp : Application(), IBaseAppInject {
    private var sApp: DemoApp? = null
    private var mCtx: Application? = null

    override fun getApplication(): Application {
        return sApp!!
    }

    override fun getNotificationIcon() = R.mipmap.ic_launcher

    override fun showTipMsg(msg: String) {
        Toast.makeText(mCtx, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate() {
        super.onCreate()

        if (!isMainProcess()) {
            return
        }

        sApp = this
        mCtx = getApplication()

        ActManager.get().apply {
            loggerConfig(true to Log.VERBOSE)
            register(this@DemoApp)
        }

    }

}