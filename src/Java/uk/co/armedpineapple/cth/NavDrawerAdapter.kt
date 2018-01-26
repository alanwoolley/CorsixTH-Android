package uk.co.armedpineapple.cth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class NavDrawerAdapter(internal val context: Context, internal val items: List<MenuItems>) : BaseAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View
        val mi = getItem(position) as MenuItems

        view = if (convertView != null) {
            convertView
        } else {
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.menu_item, parent, false)
        }
        val tv = view.findViewById<View>(R.id.menu_item_text) as TextView
        if (mi.textResource != null) {
            tv.setText(mi.textResource)
        }


        return view
    }

}
