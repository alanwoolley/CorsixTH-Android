package uk.co.armedpineapple.cth.files

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.databinding.SaveListItemBinding
import uk.co.armedpineapple.cth.files.persistence.SaveData
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * A RecyclerView adapter for presenting a list of save games.
 *
 * @property context A context
 * @property vm A SaveGameViewMdoel
 * @property showNew Whether the option for a new save should be presented
 * @property showAutosaves Whether autosaves should be presented
 * @property newItemCallback A callback for when the new item option is selected
 * @property saveItemCallback A callback for when an existing item is selected
 * @property userItemRemovedCallback A callback for when the user requests an item for deletion.
 * The adapter does not persist any deletion itself, other than to its own internal collection.
 */
class SaveLoadRecyclerViewAdapter(
    private val context: Context,
    private val vm: SaveGameViewModel,
    private val showNew: Boolean = true,
    private val showAutosaves: Boolean = true,
    private val newItemCallback: () -> Unit,
    private val saveItemCallback: (saveData: SaveData) -> Unit,
    private val userItemRemovedCallback: (position: Int, saveData: SaveData) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var values: MutableList<SaveData> = getOrderedSaves().toMutableList()

    private val dataObserver: Observer<List<SaveData>> =
        Observer { values = getOrderedSaves().toMutableList() }

    /**
     * Removes an item at a given position.
     *
     * @param position The position of the item to remove.
     */
    fun removeAt(position: Int) {
        val item = values[position];
        values.removeAt(position)
        notifyItemRemoved(position)
        userItemRemovedCallback(position, item)
    }

    /**
     * Reinserts a removed item at its original position.
     *
     * @param position The position the item was originally in.
     * @param saveData The item
     */
    fun reinsert(position: Int, saveData: SaveData) {
        values.add(position, saveData)
        notifyItemInserted(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        vm.saves.observeForever(dataObserver)
        vm.autosaves.observeForever(dataObserver)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        vm.saves.removeObserver(dataObserver)
        vm.autosaves.removeObserver(dataObserver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewTypes.SAVE.ordinal -> SaveViewHolder(
                SaveListItemBinding.inflate(
                    layoutInflater, parent, false
                )
            )

            else -> {
                NewSaveViewHolder(
                    layoutInflater.inflate(
                        R.layout.new_save_list_item, parent, false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SaveViewHolder) {
            val item = values[if (showNew) position - 1 else position]
            holder.levelView.text = item.levelName
            holder.dateView.text = item.saveDate?.format(
                DateTimeFormatter.ofLocalizedDateTime(
                    FormatStyle.MEDIUM
                )
            )
            holder.levelView.text = item.levelName
            holder.moneyView.text = item.money.toString()
            holder.repView.text = item.rep.toString()
            holder.nameView.text = item.saveName.removeSuffix(FilesService.SAVE_GAME_FILE_SUFFIX)

            item.screenshotPath?.let {
                Glide.with(context).load(File(it)).into(holder.screenshotView)
            }

            holder.itemView.setOnClickListener { saveItemCallback(item) }
        } else if (holder is NewSaveViewHolder) {
            holder.itemView.setOnClickListener { newItemCallback() }
        }
    }

    override fun getItemCount(): Int = values.size + if (showNew) 1 else 0

    private fun getOrderedSaves(): List<SaveData> {
        val saves = vm.saves.value

        val combined = if (showAutosaves) {
            val autosaves = vm.autosaves.value
            saves?.let { s ->
                autosaves?.let { a ->
                    s.plus(a)
                }
            }
        } else {
            saves
        }

        return combined?.let {
            it.sortedByDescending { sd -> sd.saveDate }
        } ?: listOf()
    }

    override fun getItemViewType(position: Int): Int {
        return if (!showNew || position > 0) ViewTypes.SAVE.ordinal
        else ViewTypes.NEWSAVE.ordinal
    }

    private enum class ViewTypes {
        SAVE,
        NEWSAVE
    }

    private inner class NewSaveViewHolder(v: View) : ViewHolder(v)

    private inner class SaveViewHolder(binding: SaveListItemBinding) : ViewHolder(binding.root) {
        val levelView: TextView = binding.level
        val moneyView: TextView = binding.money
        val repView: TextView = binding.rep
        val screenshotView: ImageView = binding.saveImage
        val nameView: TextView = binding.saveName
        val dateView: TextView = binding.saveDate
    }
}