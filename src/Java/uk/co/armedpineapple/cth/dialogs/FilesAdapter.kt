package uk.co.armedpineapple.cth.dialogs

import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.j256.ormlite.dao.Dao
import uk.co.armedpineapple.cth.FileDetails
import uk.co.armedpineapple.cth.Files
import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.Reporting
import uk.co.armedpineapple.cth.persistence.PersistenceHelper
import uk.co.armedpineapple.cth.persistence.SaveData
import java.io.File
import java.sql.SQLException

class FilesAdapter(private val items: List<FileDetails>, private val listener: FilesClickListener, persistence: PersistenceHelper) : RecyclerView.Adapter<FilesAdapter.FilesViewHolder>() {
    private var dao: Dao<SaveData, String>? = null

    init {
        try {
            this.dao = persistence.getDao(SaveData::class.java)
        } catch (e: SQLException) {
            Reporting.report(e)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.file_card, parent, false)
        return FilesViewHolder(v)
    }


    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        val details = items[position]
        holder.name.text = details.fileName.replace(".sav", "")
        holder.details = details

        if (holder.saveData == null) {
            val longPath = details.directory + File.separator + details.fileName
            try {
                val saveData = dao!!.queryForId(longPath)
                if (saveData != null) {
                    holder.saveData = saveData
                }
            } catch (e: SQLException) {
                Reporting.report(e)
            }

        }

        if (holder.saveData != null) {
            holder.level.visibility = View.VISIBLE
            holder.rep.visibility = View.VISIBLE
            holder.money.visibility = View.VISIBLE
            val res = holder.itemView.resources
            holder.level.text = res.getString(R.string.save_level_text, holder.saveData!!.levelName)
            holder.rep.text = res.getString(R.string.save_rep_text, holder.saveData!!.rep)
            holder.money.text = res.getString(R.string.save_balance_text, holder.saveData!!.money)

            if (Files.doesFileExist(holder.saveData!!.screenshotPath)) {
                holder.image.setImageBitmap(BitmapFactory.decodeFile(holder.saveData!!.screenshotPath))
            } else {
                holder.image.visibility = View.INVISIBLE
            }

        } else {
            holder.level.visibility = View.GONE
            holder.rep.visibility = View.GONE
            holder.money.visibility = View.GONE
            holder.image.visibility = View.INVISIBLE
        }

        holder.setListener(View.OnClickListener { listener.onItemClick(details) })

    }

    override fun getItemCount(): Int {
        return items.size
    }

    class FilesViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var rep: TextView = itemView.findViewById<View>(R.id.saverep) as TextView
        internal var money: TextView = itemView.findViewById<View>(R.id.savemoney) as TextView
        internal var name: TextView = itemView.findViewById<View>(R.id.savename) as TextView
        internal var level: TextView = itemView.findViewById<View>(R.id.savelevel) as TextView
        internal var image: ImageView = itemView.findViewById<View>(R.id.saveimage) as ImageView
        internal var details: FileDetails? = null
        internal var saveData: SaveData? = null


        internal fun setListener(clickListener: View.OnClickListener) {
            itemView.setOnClickListener(clickListener)
        }

    }

    interface FilesClickListener {
        fun onItemClick(details: FileDetails)
    }

}
