/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.language_list_item.view.*
import kotlinx.android.synthetic.main.wizard_language.view.*
import uk.co.armedpineapple.cth.Configuration
import uk.co.armedpineapple.cth.Configuration.ConfigurationException
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting
import java.util.*

class LanguageWizard(private val ctx: Context, attrs: AttributeSet) : WizardView(ctx, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (!isInEditMode) {

            val langArray = resources.getStringArray(R.array.languages)
            val langValuesArray = resources.getStringArray(
                    R.array.languages_values)

            languageListView.adapter = LanguageListAdapter(ctx, langArray,
                    langValuesArray)

            languageListView.onItemClickListener = OnItemClickListener { arg0, arg1, pos, arg3 -> (arg0.adapter as LanguageListAdapter).selected = pos }
            // Look for the language in the values array
            Log.d("System Language: " + Locale.getDefault().language)

            for (i in langValuesArray.indices) {
                if (langValuesArray[i] == Locale.getDefault().language) {
                    (languageListView.adapter as LanguageListAdapter)
                            .selected = i
                    break
                }
            }
        }
    }

    @Throws(ConfigurationException::class)
    override fun saveConfiguration(config: Configuration) {

        val lang = (languageListView.adapter as LanguageListAdapter)
                .selectedItem as String

        config.language = lang

    }

    override fun loadConfiguration(config: Configuration) {

    }

    internal inner class LanguageListAdapter(ctx: Context, val text: Array<String>,
                                             val values: Array<String>) : BaseAdapter() {

        private val inflater: LayoutInflater = LayoutInflater.from(ctx)

        var selected: Int = 0
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        val selectedItem: Any
            get() = getItem(selected)

        override fun getCount(): Int {
            return text.size
        }

        override fun getItem(position: Int): Any = values[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val newView: View = convertView
                    ?: inflater.inflate(R.layout.language_list_item, parent, false)

            val langFlagsArray = resources.obtainTypedArray(
                    R.array.languages_flags)
            newView.languageListFlag.setImageDrawable(langFlagsArray.getDrawable(position))
            langFlagsArray.recycle()
            newView.languageText.text = text[position]

            newView.languageListTick.visibility = if (selected == position) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }


            return newView
        }
    }

    companion object {
        private val Log = Reporting.getLogger("LanguageWizard")
    }

}
