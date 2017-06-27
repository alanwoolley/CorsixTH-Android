// Copyright (C) 2012 Alan Woolley
// 
// See LICENSE.TXT for full license
//
package uk.co.armedpineapple.cth;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public abstract class CTHActivity extends AppCompatActivity {

	private static final String	LOG_TAG	= "CTHActivity";

	public CTHApplication				app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (CTHApplication) getApplication();

	}
}
