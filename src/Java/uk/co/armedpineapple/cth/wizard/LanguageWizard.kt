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
import android.widget.ImageView
import android.widget.TextView
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

            languageListView.onItemClickListener = OnItemClickListener { arg0, arg1, pos, arg3 -> (arg0.adapter as LanguageListAdapter).setSelectedItem(pos) }
            // Look for the language in the values array
            Log.d("System Language: " + Locale.getDefault().language)

            for (i in langValuesArray.indices) {
                if (langValuesArray[i] == Locale.getDefault().language) {
                    (languageListView.adapter as LanguageListAdapter)
                            .setSelectedItem(i)
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

        val inflater: LayoutInflater = LayoutInflater.from(ctx)

        var selected = 0

        val selectedItem: Any
            get() = getItem(selected)

        override fun getCount(): Int {
            return text.size
        }

        override fun getItem(position: Int): Any {
            return values[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        fun setSelectedItem(pos: Int) {
            selected = pos
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val newView: View = convertView
                    ?: inflater.inflate(R.layout.language_list_item, parent, false)

            val langFlagsArray = resources.obtainTypedArray(
                    R.array.languages_flags)
            (newView.findViewById<View>(R.id.language_flag) as ImageView)
                    .setImageDrawable(langFlagsArray.getDrawable(position))
            langFlagsArray.recycle()
            (newView.findViewById<View>(R.id.language_text) as TextView).text = text[position]

            if (selected == position) {
                newView.findViewById<View>(R.id.language_tick).visibility = View.VISIBLE
            } else {
                newView.findViewById<View>(R.id.language_tick).visibility = View.INVISIBLE
            }

            return newView
        }
    }

    companion object {
        private val Log = Reporting.getLogger("LanguageWizard")
    }

}
