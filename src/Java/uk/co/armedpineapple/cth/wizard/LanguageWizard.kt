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
import uk.co.armedpineapple.cth.models.Language
import uk.co.armedpineapple.cth.services.LanguageService
import java.lang.Math.max
import java.util.*

class LanguageWizard(private val ctx: Context, attrs: AttributeSet) : WizardView(ctx, attrs) {

    private lateinit var languageService : LanguageService

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {

            languageService = LanguageService(ctx)


            languageListView.adapter = LanguageListAdapter(ctx, languageService.languages)

            languageListView.onItemClickListener = OnItemClickListener { arg0, _, pos, _ -> (arg0.adapter as LanguageListAdapter).selected = pos }
            // Look for the language in the values array
            Log.d("System Language: " + Locale.getDefault().language)

            (languageListView.adapter as LanguageListAdapter).selected = max(0, languageService.languages.indexOfFirst { l -> l.code == languageService.userLanguage })
        }
    }

    @Throws(ConfigurationException::class)
    override fun saveConfiguration(config: Configuration) {
        languageService.userLanguage = (languageListView.adapter as LanguageListAdapter).selectedItem.code
//        val lang = (languageListView.adapter as LanguageListAdapter)
//                .selectedItem as String
//        config.language = lang
    }

    override fun loadConfiguration(config: Configuration) {

    }

    internal inner class LanguageListAdapter(ctx: Context, val languages: List<Language>) : BaseAdapter() {

        private val inflater: LayoutInflater = LayoutInflater.from(ctx)

        var selected: Int = 0
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        val selectedItem: Language
            get() = getItem(selected) as Language

        override fun getCount(): Int {
            return languages.size
        }

        override fun getItem(position: Int): Any = languages[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val newView: View = convertView
                    ?: inflater.inflate(R.layout.language_list_item, parent, false)

            val langFlagsArray = resources.obtainTypedArray(
                    R.array.languages_flags)
            newView.languageListFlag.setImageDrawable(langFlagsArray.getDrawable(position))
            langFlagsArray.recycle()
            newView.languageListText.text = languages[position].displayName

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
