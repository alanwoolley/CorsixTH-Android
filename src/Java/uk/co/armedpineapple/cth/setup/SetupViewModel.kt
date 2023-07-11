package uk.co.armedpineapple.cth.setup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.runOnUiThread
import uk.co.armedpineapple.cth.CTHApplication
import uk.co.armedpineapple.cth.files.FilesService
import uk.co.armedpineapple.innoextract.service.Configuration
import uk.co.armedpineapple.innoextract.service.ExtractCallback
import uk.co.armedpineapple.innoextract.service.IExtractService

class SetupViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {

    private val filesService: FilesService = FilesService(application)

    private val mutableIsExtracting: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val mutableExtractProgress: MutableLiveData<Int> = MutableLiveData<Int>()
    private val mutableInstallerStatus: MutableLiveData<InstallerValidationResult> =
        MutableLiveData<InstallerValidationResult>()
    private val mutableExtractResult: MutableLiveData<ExtractResult> =
        MutableLiveData<ExtractResult>()

    val isExtracting: LiveData<Boolean> = mutableIsExtracting
    val extractProgress: LiveData<Int> = mutableExtractProgress
    val installerStatus: LiveData<InstallerValidationResult> = mutableInstallerStatus
    val extractResult: LiveData<ExtractResult> = mutableExtractResult

    fun onGameSourceTreeGranted(uri: Uri) {
        val application = getApplication<CTHApplication>()

        mutableIsExtracting.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val documentRoot = DocumentFileCompat.fromTreeUri(application, uri)
                documentRoot?.let {
                    val progressChannel =
                        Channel<FilesService.EstimatedFileOperationProgress>(Channel.CONFLATED) { progress ->
                            val adjusted = (progress.progress * 100).toInt()
                            mutableExtractProgress.postValue(adjusted)
                        }
                    filesService.installOriginalFiles(
                        documentRoot, application.configuration, progressChannel
                    )
                }
            }
            mutableIsExtracting.value = false
            mutableExtractProgress.value = 100
            mutableExtractResult.value = ExtractResult.SUCCESS
        }
    }

    fun onGameInstallerGranted(uri: Uri, extractService: IExtractService) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val innoValidationResult = extractService.check(uri)
                val installerResult = when {
                    innoValidationResult.isValid && innoValidationResult.isGogInstaller && (innoValidationResult.gogId != GOG_GAME_ID) -> InstallerValidationResult.NOT_THEME_HOSPITAL
                    innoValidationResult.isValid && innoValidationResult.isGogInstaller -> InstallerValidationResult.VALID
                    else -> InstallerValidationResult.INVALID
                }
                mutableInstallerStatus.postValue(installerResult)

                if (installerResult == InstallerValidationResult.VALID) {
                    extractInstaller(uri, extractService)
                }
            }
        }
    }

    private fun extractInstaller(uri: Uri, extractService: IExtractService) {
        mutableIsExtracting.postValue(true)
        val application = getApplication<CTHApplication>()
        val extractCallback = object : ExtractCallback {
            override fun onFailure(e: Exception) {
                mutableIsExtracting.postValue(false)
                mutableExtractResult.postValue(ExtractResult.FAILURE)
            }

            override fun onProgress(value: Long, max: Long, file: String) {
                mutableExtractProgress.postValue((value * 100 / max).toInt())
            }

            override fun onSuccess() {
                mutableIsExtracting.postValue(false)
                mutableExtractResult.postValue(ExtractResult.SUCCESS)
            }

        }
        filesService.nukeOriginalFiles((application.configuration))
        
        extractService.extract(
            uri, application.configuration.thFiles, extractCallback, Configuration(
                showOngoingNotification = false, showFinalNotification = false
            )
        )
    }

    companion object {
        private const val GOG_GAME_ID = 1207659026L

        enum class InstallerValidationResult {
            INVALID, NOT_THEME_HOSPITAL, VALID
        }

        enum class ExtractResult {
            FAILURE, SUCCESS
        }
    }
}