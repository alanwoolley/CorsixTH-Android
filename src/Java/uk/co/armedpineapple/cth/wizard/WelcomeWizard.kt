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

class WelcomeWizard : WizardView {

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)

    @Throws(ConfigurationException::class)
    override fun saveConfiguration(config: Configuration) {

    }

    override fun loadConfiguration(config: Configuration) {

    }

}
