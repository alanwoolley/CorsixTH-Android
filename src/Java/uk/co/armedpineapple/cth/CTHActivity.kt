// Copyright (C) 2012 Alan Woolley
// 
// See LICENSE.TXT for full license
//
package uk.co.armedpineapple.cth

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class CTHActivity : AppCompatActivity() {

    lateinit var app: CTHApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as CTHApplication
    }

}
