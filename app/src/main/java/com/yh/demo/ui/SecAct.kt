package com.yh.demo.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.yh.demo.R

/**
 * Created by CYH on 2020-03-16 15:57
 */
class SecAct : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.act_content)
    }

    fun goNext(view: View) {
        startActivity(Intent(this, ThirdAct::class.java))
    }
}