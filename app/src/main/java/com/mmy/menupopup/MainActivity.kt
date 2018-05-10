package com.mmy.menupopup

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.mmy.kotlinsample.popup.ControlPopup
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val ints = IntArray(5)
        ints[0] = R.mipmap.ic_launcher
        ints[1] = R.mipmap.ic_launcher
        ints[2] = R.mipmap.ic_launcher
        ints[3] = R.mipmap.ic_launcher
        ints[4] = R.mipmap.ic_launcher
        Handler().postDelayed({
            ControlPopup(this, v_view, ints).show(this)
        }, 1000)
    }
}
