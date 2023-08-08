package uk.co.armedpineapple.cth.setup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import uk.co.armedpineapple.cth.GameActivity
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {

    companion object {
        fun newInstance() = SetupFragment()
    }

    private lateinit var viewModel: SetupViewModel
    private lateinit var binding: FragmentSetupBinding
    private lateinit var documentTreeResultHandler: ActivityResultLauncher<Uri?>
    private lateinit var documentFileResultHandler: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        val openDocumentTreeContract = ActivityResultContracts.OpenDocumentTree()
        val openFileContract = ActivityResultContracts.OpenDocument()
        documentTreeResultHandler =
            registerForActivityResult(openDocumentTreeContract) { uri: Uri? ->
                uri?.let {
                    viewModel.onGameSourceTreeGranted(it)
                    Toast.makeText(requireActivity(), "Copying. Please wait.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        documentFileResultHandler = registerForActivityResult(openFileContract) { uri: Uri? ->
            val activity = requireActivity()
            val service = (activity as SetupActivity).extractService
            uri?.let { viewModel.onGameInstallerGranted(it, service) }
        }

        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        viewModel.extractResult.observe(this, ::onExtractResultChanged)
        viewModel.installerStatus.observe(this, ::onInstallerStatusChanged)
    }

    override fun onPause() {
        super.onPause()
        viewModel.extractResult.removeObserver(::onExtractResultChanged)
        viewModel.installerStatus.removeObserver(::onInstallerStatusChanged)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSetupBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.vm = viewModel
        binding.fragment = this
        return binding.root
    }

    fun importDirectory() {
        documentTreeResultHandler.launch(null)
    }

    fun downloadAndExtractDemo() {
        viewModel.downloadAndExtractDemo()
    }

    fun importFromInstaller() {
        documentFileResultHandler.launch(
            arrayOf(
                "application/octet-stream",
                "application/vnd.microsoft.portable-executable",
                "application/x-msdownload",
                "application/x-executable",
                "application/x-msdos-program"
            )
        )
    }

    private fun onExtractResultChanged(result: SetupViewModel.Companion.ExtractResult) {
        when (result) {
            SetupViewModel.Companion.ExtractResult.FAILURE -> Toast.makeText(
                requireActivity(), getString(R.string.loading_theme_hospital_failed), Toast.LENGTH_LONG
            ).show()

            SetupViewModel.Companion.ExtractResult.SUCCESS -> startGameActivity()
        }
    }

    private fun onInstallerStatusChanged(result: SetupViewModel.Companion.InstallerValidationResult?) {
        when (result) {
            SetupViewModel.Companion.InstallerValidationResult.INVALID -> Toast.makeText(
                requireActivity(), getString(R.string.unrecognised_file), Toast.LENGTH_SHORT
            ).show();
            SetupViewModel.Companion.InstallerValidationResult.NOT_THEME_HOSPITAL -> Toast.makeText(
                requireActivity(),
                getString(R.string.invalid_gog_installer_are_you_sure_this_is_theme_hospital),
                Toast.LENGTH_SHORT
            ).show()

            SetupViewModel.Companion.InstallerValidationResult.VALID -> Toast.makeText(
                requireActivity(), getString(R.string.installing_please_wait), Toast.LENGTH_SHORT
            ).show()

            else -> {}
        }
    }

    private fun startGameActivity() {
        val intent = Intent(requireActivity(), GameActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        requireActivity().finish()
    }
}