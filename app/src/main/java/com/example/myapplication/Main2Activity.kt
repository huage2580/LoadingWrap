package com.example.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        LoadingWrap.toLoadingStatus(button,LoadingWrap.SIZE.SMALL,true)
        button.setOnClickListener {
            button.toStringStatus()
        }
    }
}
