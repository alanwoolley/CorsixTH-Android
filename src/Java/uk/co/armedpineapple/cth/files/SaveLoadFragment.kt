package uk.co.armedpineapple.cth.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.persistence.saves.SaveData
import kotlin.properties.Delegates


/**
 * A fragment for saving or loading a saved game from a list of saved games.
 */
class SaveLoadFragment : Fragment() {

    private lateinit var viewModel: SaveGameViewModel
    private var isLoad by Delegates.notNull<Boolean>()
    private val columnCount = 1
    private var adapter: SaveLoadRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isLoad = arguments?.getBoolean(BUNDLE_ISLOAD, false) ?: false;

        viewModel = ViewModelProvider(this)[SaveGameViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save_load_list, container, false)

        val newSaveDialogView = inflater.inflate(R.layout.dialog_newsave, null)

        val newSaveDialog = AlertDialog.Builder(requireContext()).setView(newSaveDialogView)
            .setTitle(R.string.new_save).setPositiveButton(R.string.save) { _, _ ->
                val name =
                    newSaveDialogView.findViewById<EditText>(R.id.newSaveName).text.toString()
                setFragmentResult(
                    REQUEST_SELECT,
                    bundleOf(BUNDLE_FILENAME to name + FilesService.SAVE_GAME_FILE_SUFFIX)
                )
            }.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }.create()

        // Set the adapter
        if (view is RecyclerView) {
            val newAdapter = SaveLoadRecyclerViewAdapter(
                context = requireContext(),
                vm = viewModel,
                showNew = !isLoad,
                showAutosaves = isLoad,
                newItemCallback = { newSaveDialog.show() },
                saveItemCallback = {
                    if (isLoad) {
                        setFragmentResult(
                            REQUEST_SELECT, bundleOf(BUNDLE_FILENAME to it.saveName)
                        )
                    } else {
                        showConfirmDialog(it)

                    }
                },
                userItemRemovedCallback = ::onSaveDeleteRequested
            )
            this.adapter = newAdapter;
            val itemTouchHelper =
                ItemTouchHelper(SwipeToDeleteCallback(requireContext(), newAdapter))

            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                adapter = this@SaveLoadFragment.adapter
                itemTouchHelper.attachToRecyclerView(view)
            }
        }

        return view
    }

    private fun onSaveDeleteRequested(position: Int, saveData: SaveData) {
        Snackbar.make(
            requireActivity().window.decorView, String.format(
                getString(R.string.s_deleted),
                saveData.saveName.removeSuffix(FilesService.SAVE_GAME_FILE_SUFFIX)
            ), Snackbar.LENGTH_LONG
        ).addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event != DISMISS_EVENT_ACTION) {
                        viewModel.deleteSave(saveData)
                    }
                }
            }).setAction(getString(R.string.undo)) { adapter?.reinsert(position, saveData) }.show();
    }

    private fun showConfirmDialog(saveData: SaveData) {
        val name = saveData.saveName.removeSuffix(FilesService.SAVE_GAME_FILE_SUFFIX)
        AlertDialog.Builder(requireContext()).setTitle(getString(R.string.overwrite_save))
            .setMessage(
                String.format(
                    getString(R.string.this_will_overwrite_saved_game_s_are_you_sure), name
                )
            ).setPositiveButton(getString(R.string.save)) { _, _ ->
                setFragmentResult(
                    REQUEST_SELECT, bundleOf(BUNDLE_FILENAME to saveData.saveName)
                )
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            .create().show()
    }

    companion object {
        const val REQUEST_SELECT = "request_select"
        const val BUNDLE_FILENAME = "bundle_filename"
        const val BUNDLE_ISLOAD = "bundle_isload"

        /**
         * Creates a new instance of SaveLoadFragment
         *
         * @param isLoad Whether the fragment is used for loading or saving a game.
         * @return A new SaveLoadFragment
         */
        fun newInstance(isLoad: Boolean): SaveLoadFragment {
            val fragment = SaveLoadFragment()
            val args = Bundle()
            args.putBoolean(BUNDLE_ISLOAD, isLoad)
            fragment.arguments = args
            return fragment
        }
    }
}