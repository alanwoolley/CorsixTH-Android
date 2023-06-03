package uk.co.armedpineapple.cth.services

import android.content.Context
import org.jetbrains.anko.defaultSharedPreferences
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.models.Language
import java.util.*

class LanguageService(private val applicationContext : Context) {

    var userLanguage : String
        get() = preferences.getString(LANGUAGE_KEY, systemLocale.language)!!
        set(value){
            preferences.edit().putString(LANGUAGE_KEY, value).apply()
        }

    val languages : List<Language> by lazy{
        val names = applicationContext.resources.getStringArray(R.array.languages)
        val codes = applicationContext.resources.getStringArray(R.array.languages_values)

        names.zip(codes).map { (name, code) -> Language(name, code) }
    }

    private val systemLocale = Locale.getDefault()
    private val preferences = applicationContext.defaultSharedPreferences

    companion object {
        private const val LANGUAGE_KEY = "language_pref"
    }
}