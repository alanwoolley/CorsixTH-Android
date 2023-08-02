package uk.co.armedpineapple.cth.files

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.co.armedpineapple.cth.CTHApplication
import uk.co.armedpineapple.cth.files.persistence.SaveData
import java.io.File
import java.time.Instant
import java.time.ZoneId

/**
 * View model for save games
 *
 * @param application The application
 */
class SaveGameViewModel(application: Application) : AndroidViewModel(application) {

    private val saveDao = getApplication<CTHApplication>().database.saveDao()
    private val filesService = getApplication<CTHApplication>().filesService
    private val config = getApplication<CTHApplication>().configuration
    private val mutableSaves = MutableLiveData<List<SaveData>>()
    private val mutableAutosaves = MutableLiveData<List<SaveData>>()

    val saves: LiveData<List<SaveData>> = mutableSaves
    val autosaves: LiveData<List<SaveData>> = mutableAutosaves

    init {
        onSavesUpdated()
    }

    /**
     * To be called when saved games have been updated.
     */
    fun onSavesUpdated() {
        CoroutineScope(Dispatchers.IO).launch {
            mutableSaves.postValue(getSaves())
            mutableAutosaves.postValue(getAutosaves())
        }
    }

    /**
     * Deletes a save game and all its related stored data.
     *
     * @param saveData The SaveData
     */
    fun deleteSave(saveData: SaveData) {
        saveData.screenshotPath?.let {
            val screenshotFile = File(it)
            if (screenshotFile.exists()) screenshotFile.delete()
        }

        val gameFile = filesService.getSaveFile(saveData.saveName, config)
        if (gameFile.exists()) gameFile.delete()

        CoroutineScope(Dispatchers.IO).launch {
            saveDao.delete(saveData)
            onSavesUpdated()
        }
    }

    private fun getSaves(): List<SaveData> {
        val saveFiles = filesService.getSaveGameFiles(config)
        return associateSaves(saveFiles)
    }

    private fun getAutosaves(): List<SaveData> {
        val saveFiles = filesService.getAutoSaveGameFiles(config)
        return associateSaves(saveFiles)
    }

    private fun associateSaves(saveFiles: Array<out File>): List<SaveData> {
        val saveRecords = saveDao.getAll().associateBy { it.saveName }

        return saveFiles.map { file ->
            saveRecords.getOrElse(file.name) { SaveData(file.name) }.also {
                it.saveDate =
                    Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
            }
        }
    }
}
