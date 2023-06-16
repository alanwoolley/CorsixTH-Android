package uk.co.armedpineapple.cth.setup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import uk.co.armedpineapple.cth.GameActivity
import uk.co.armedpineapple.cth.databinding.FragmentSetupBinding
import uk.co.armedpineapple.cth.settings.SettingsActivity

class SetupFragment : Fragment() {

    companion object {
        fun newInstance() = SetupFragment()
    }

    private lateinit var viewModel: SetupViewModel
    private lateinit var binding: FragmentSetupBinding
    private lateinit var resultHandler: ActivityResultLauncher<Uri?>

    override fun onCreate(savedInstanceState: Bundle?) {
        val contract = ActivityResultContracts.OpenDocumentTree()
        resultHandler = registerForActivityResult(contract) { uri: Uri? ->
            uri?.let { viewModel.onGameSourceAccessGranted(it) }
            startGameActivity();
        }

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupBinding.inflate(inflater)
        binding.buttonImport.setOnClickListener { pickGameSource() }
        return binding.root
    }

    private fun pickGameSource() {
        startGameActivity();
        //resultHandler.launch(null)
    }
    private fun startGameActivity() {
        val intent = Intent(requireActivity(), GameActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        requireActivity().finish()
    }

}