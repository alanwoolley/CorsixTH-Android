package uk.co.armedpineapple.cth.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

import uk.co.armedpineapple.cth.R
import uk.co.armedpineapple.cth.SDLActivity

class HelpDialog(context: SDLActivity) : Dialog(context), OnItemClickListener {

    private val mHelpListAdapter: HelpListAdapter
    private val mHelpListView: ListView
    private val ctx: Context

    init {
        this.ctx = context
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.help_dialog)

        mHelpListView = findViewById<View>(R.id.help_list) as ListView
        mHelpListAdapter = HelpListAdapter(context, context.resources
                .getStringArray(R.array.help_list), context.resources
                .getStringArray(R.array.help_values))
        mHelpListView.adapter = mHelpListAdapter
        mHelpListView.onItemClickListener = this
    }

    override fun onItemClick(adapterview: AdapterView<*>, arg1: View, pos: Int, arg3: Long) {
        val intentUrl = mHelpListAdapter.getItem(pos) as String
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(intentUrl))
        ctx.startActivity(browserIntent)

    }

    internal inner class HelpListAdapter(ctx: Context, var text: Array<String>, var urls: Array<String>) : BaseAdapter() {

        var inflater: LayoutInflater = LayoutInflater.from(ctx)
        var selected = 0

        override fun getCount(): Int {
            return text.size
        }

        override fun getItem(position: Int): Any {
            return urls[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val newView: View

            if (convertView == null) {
                newView = inflater.inflate(R.layout.help_item, parent, false)
            } else {
                newView = convertView
            }

            (newView.findViewById<View>(R.id.help_item_text) as TextView).text = text[position]

            return newView
        }
    }


}
