package uk.co.armedpineapple.cth.localisation

import android.content.Context
import androidx.core.os.LocaleListCompat
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import uk.co.armedpineapple.cth.R
import java.util.Locale

/**
 * Language service
 *
 * @property ctx  context
 * @constructor Create Language service
 */
class LanguageService(private val ctx: Context) : AnkoLogger {

    /**
     * Gets a default CTH language that matches a locale from the app's resource configuration.
     *
     * This makes a best guess attempt at matching a language from the list of supported CTH
     * languages. If a well matching language cannot be determined, then English is used.
     *
     * @return a CTH language matching the user's locale preferences.
     */
    fun getCthLanguageFromAppConfig(): String {
        val originalLangs = ctx.resources.getStringArray(R.array.languages_values)
        val langs = originalLangs.clone()

        // All of the CTH language identifiers match the language tags
        // apart from Chinese. A simple substitution allows us to pick
        // these up.
        langs[langs.indexOf("zh(s)")] = "zh_hans"
        langs[langs.indexOf("zh(t)")] = "zh_hant"

        var result: String? = null
        try {
            val mappedGameLocales = ArrayList<Locale>()
            for (lang in langs) {
                val asLangTag = lang.replace("_", "-")
                // Convert the CTH language identifier into a locale.
                mappedGameLocales.add(Locale.forLanguageTag(asLangTag))
            }

            val locales = ctx.resources.configuration.locales

            // Look for a locale that matches the language and script of the CTH-derived locales
            // and pick the first one.
            localeSearch@ for (i in 0..locales.size()) {
                for (j in 0..mappedGameLocales.count()) {
                    if (LocaleListCompat.matchesLanguageAndScript(
                            locales[i], mappedGameLocales[j]
                        )
                    ) {
                        result = originalLangs[j]
                        break@localeSearch
                    }
                }
            }
        } catch (e: Exception) {
            warn { "Couldn't discover locale. Defaulting to English. ${e.message}" }
        }

        // If we can't work it out, default to English.
        return result ?: ctx.getString(R.string.cth_lang_english)
    }
}