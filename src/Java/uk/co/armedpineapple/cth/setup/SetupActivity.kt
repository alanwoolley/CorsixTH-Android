package uk.co.armedpineapple.cth.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import uk.co.armedpineapple.cth.R

class SetupActivity : AppCompatActivity() {

    private lateinit var viewModel: SetupViewModel
    private val PICK_SOURCE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        viewModel = ViewModelProvider(this).get(SetupViewModel::class.java)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SetupFragment.newInstance()).commitNow()
        }
    }

    fun openDirectoryBrowser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

        var contract = ActivityResultContracts.OpenDocumentTree()
    }


}