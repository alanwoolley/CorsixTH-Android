/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard

import uk.co.armedpineapple.cth.Configuration
import uk.co.armedpineapple.cth.Configuration.ConfigurationException

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

abstract class WizardView : RelativeLayout {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    /**
     * Stores the wizard settings in a configuration. This is called when the
     * wizard page is navigated away from
     */
    @Throws(ConfigurationException::class)
    abstract fun saveConfiguration(config: Configuration)

    /**
     * Populates the wizard using a configuration. Called when the wizard page is
     * attached
     */
    abstract fun loadConfiguration(config: Configuration)

}
