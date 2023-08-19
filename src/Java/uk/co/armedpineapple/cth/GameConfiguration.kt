package uk.co.armedpineapple.cth

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Keep
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.PrintWriter
import kotlin.reflect.KProperty
import kotlin.reflect.full.starProjectedType


class GameConfiguration(private val ctx: Context, private val preferences: SharedPreferences) :
    Loggable {

    val cthFiles: File = File(ctx.noBackupFilesDir, "cth")
    val cthLaunchScript = File(cthFiles, "CorsixTH.lua")
    val gameConfigFile = File(cthFiles, "config.txt")
    val thFiles: File = File(ctx.noBackupFilesDir, "themehospital")
    val saveFiles: File = File(ctx.filesDir, "saves")
    val autosaveFiles: File = File(saveFiles, "Autosaves")
    val screenshots: File = File(ctx.filesDir, "screenshots")
    val musicLib: File = File(ctx.noBackupFilesDir, "timidity")

    val unicodeFont = File(ctx.noBackupFilesDir, "fonts/DroidSansFallbackFull.ttf")

    var resolution: Pair<UInt, UInt> =
        decodeResolution(getStringPref(R.string.prefs_display_resolution).toUInt())
    val fullscreen = true

    @get:Keep
    val language: String by createReadOnlyOption(R.string.prefs_language)

    @get:Keep
    val advisorEnabled: Boolean by createReadOnlyOption(R.string.prefs_gameplay_advisor)

    @get:Keep
    val sfxEnabled: Boolean by createReadOnlyOption(R.string.prefs_audio_sounds)

    @get:Keep
    val musicEnabled: Boolean by createReadOnlyOption(R.string.prefs_audio_music)

    @get:Keep
    val announcerEnabled: Boolean by createReadOnlyOption(R.string.prefs_audio_announcements)

    @get:Keep
    val audioEnabled: Boolean by createReadOnlyOption(R.string.prefs_audio_global)

    @get:Keep
    val sfxVolume: Int by createReadOnlyOption(R.string.prefs_audio_sounds_volume)

    @get:Keep
    val musicVolume: Int by createReadOnlyOption(R.string.prefs_audio_music_volume)

    @get:Keep
    val announcerVolume: Int by createReadOnlyOption(R.string.prefs_audio_announcements_volume)


    fun persist() {
        // Create save game directory if it doesn't already exist, otherwise CTH will use its own.

        saveFiles.mkdirs()
        val tokenMap = hashMapOf<String, String>()

        tokenMap.putAll(getStringPrefs(arrayOf(R.string.prefs_language)))
        tokenMap.putAll(
            getBoolPrefs(
                arrayOf(
                    R.string.prefs_gameplay_24hr_clock,
                    R.string.prefs_advanced_debug_mode,
                    R.string.prefs_advanced_alien_can_knock,
                    R.string.prefs_advanced_allow_blocking,
                    R.string.prefs_advanced_alien_must_stand,
                    R.string.prefs_advanced_alien_emergency_only,
                    R.string.prefs_advanced_show_fps,
                    R.string.prefs_advanced_show_machine_strength,
                    R.string.prefs_advanced_show_room_information,
                    R.string.prefs_audio_announcements,
                    R.string.prefs_audio_global,
                    R.string.prefs_audio_sounds,
                    R.string.prefs_audio_music,
                    R.string.prefs_gameplay_advisor,
                    R.string.prefs_gameplay_auto_wage,
                    R.string.prefs_gameplay_free_build,
                    R.string.prefs_gameplay_remember_room_contents,
                    R.string.prefs_gameplay_remove_destroyed_rooms,
                    R.string.prefs_video_global,
                    R.string.prefs_video_intro
                )
            )
        )

        tokenMap["disable_fractured_bones_females"] =
            (!getBoolPref(R.string.prefs_advanced_fractured_bones_allow_female)).toString()
        tokenMap["adviser_disabled"] = (!getBoolPref(R.string.prefs_gameplay_advisor)).toString()
        tokenMap["prevent_edge_scrolling"] =
            (!getBoolPref(R.string.prefs_input_edge_scrolling)).toString()

        tokenMap["th_path"] = thFiles.absolutePath
        tokenMap["save_path"] = saveFiles.absolutePath
        tokenMap["screenshots_path"] = screenshots.absolutePath
        tokenMap["unicode_font_path"] = unicodeFont.absolutePath
        tokenMap["res_w"] = resolution.first.toString()
        tokenMap["res_h"] = resolution.second.toString()
        tokenMap["fullscreen"] = fullscreen.toString()

        val templateIn = ctx.resources.openRawResource(R.raw.config_template)

        cthFiles.mkdirs()
        BufferedReader(InputStreamReader(templateIn)).useLines { lines ->
            PrintWriter(BufferedOutputStream(FileOutputStream(gameConfigFile))).use { writer ->
                lines.map { line ->
                    var newLine = line
                    tokenMap.forEach { (variable, substitute) ->
                        newLine = newLine.replace("{{$variable}}", substitute)
                    }
                    newLine
                }.forEach { replacedLine ->
                    writer.write(replacedLine + "\n")
                }
            }
        }
    }

    private fun decodeResolution(encodedValue: UInt): Pair<UInt, UInt> {
        val width = (encodedValue shr 16)
        val height = (encodedValue and 0xFFFFu)
        return Pair(width, height)
    }

    private fun getBoolPref(prefId: Int): Boolean {
        return preferences.getBoolean(ctx.getString(prefId), false)
    }

    private fun getIntPref(prefId: Int): Int {
        return preferences.getInt(ctx.getString(prefId), 0)
    }

    private fun getStringPref(prefId: Int): String {
        return preferences.getString(ctx.getString(prefId), "") as String
    }

    private fun getStringPrefs(prefs: Array<Int>) = sequence {
        for (pref in prefs) {
            val prefName = ctx.getString(pref)
            yield(Pair(prefName, preferences.getString(ctx.getString(pref), "") as String))
        }
    }

    private fun getBoolPrefs(prefs: Array<Int>) = sequence {
        for (pref in prefs) {
            val prefName = ctx.getString(pref)
            yield(Pair(prefName, preferences.getBoolean(ctx.getString(pref), false).toString()))
        }
    }

    private fun <T, V> createReadOnlyOption(prefId: Int): ReadOnlyConfigOption<T, V> {
        return ReadOnlyConfigOption(ctx, prefId, preferences)
    }

    private fun <T, V> createReadWriteOption(prefId: Int): ReadWriteConfigOption<T, V> {
        return ReadWriteConfigOption(ctx, prefId, preferences)
    }

    class ReadWriteConfigOption<in T, V>(pref: String, preferences: SharedPreferences) :
        ReadOnlyConfigOption<T, V>(pref, preferences), kotlin.properties.ReadWriteProperty<T, V> {

        constructor(
            context: Context, prefId: Int, preferences: SharedPreferences
        ) : this(context.getString(prefId), preferences)

        override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
            when (val returnType = property.returnType) {
                String::class.starProjectedType -> preferences.edit()
                    .putString(pref, value as String).apply()

                Boolean::class.starProjectedType -> preferences.edit()
                    .putBoolean(pref, value as Boolean).apply()

                Float::class.starProjectedType -> preferences.edit().putFloat(pref, value as Float)
                    .apply()

                Int::class.starProjectedType -> preferences.edit().putInt(pref, value as Int)
                    .apply()

                Long::class.starProjectedType -> preferences.edit().putLong(pref, value as Long)
                    .apply()

                else -> throw IllegalArgumentException("Unsupported type: $returnType")
            }
        }
    }

    open class ReadOnlyConfigOption<in T, V>(val pref: String, val preferences: SharedPreferences) :
        kotlin.properties.ReadOnlyProperty<T, V> {

        constructor(
            context: Context, prefId: Int, preferences: SharedPreferences
        ) : this(context.getString(prefId), preferences)

        override fun getValue(thisRef: T, property: KProperty<*>): V {
            return when (val returnType = property.returnType) {
                String::class.starProjectedType -> preferences.getString(pref, null) as V
                Boolean::class.starProjectedType -> preferences.getBoolean(pref, false) as V
                Float::class.starProjectedType -> preferences.getFloat(pref, 0f) as V
                Int::class.starProjectedType -> preferences.getInt(pref, 0) as V
                Long::class.starProjectedType -> preferences.getLong(pref, 0L) as V
                else -> throw IllegalArgumentException("Unsupported type: ${returnType}")
            }
        }

    }
}
