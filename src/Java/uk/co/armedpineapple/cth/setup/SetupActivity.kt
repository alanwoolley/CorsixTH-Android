package uk.co.armedpineapple.cth.setup

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import uk.co.armedpineapple.cth.CTHApplication
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting
import uk.co.armedpineapple.innoextract.service.ExtractService

class SetupActivity : AppCompatActivity() {

    private lateinit var viewModel: SetupViewModel
    lateinit var extractService: ExtractService
    private var serviceInitialized: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            val binder = service as ExtractService.ServiceBinder
            extractService = binder.service
            serviceInitialized = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            serviceInitialized = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SetupFragment.newInstance()).commitNow()

            val application = application as CTHApplication
            if (!application.reporting.hasRequestedConsent()) {
                application.reporting.requestConsent(this)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService.
        Intent(this, ExtractService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        serviceInitialized = false
    }

    fun openDirectoryBrowser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

        var contract = ActivityResultContracts.OpenDocumentTree()
    }
}