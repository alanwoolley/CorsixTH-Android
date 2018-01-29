package uk.co.armedpineapple.cth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.menu_item.view.*

class NavDrawerAdapter(internal val context: Context, internal val items: List<MenuItems>) : BaseAdapter() {

    private val inflater: LayoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): MenuItems {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view: View = convertView
                ?: inflater.inflate(R.layout.menu_item, parent, false)

        val mi = getItem(position)

        if (mi.textResource != null) {
            view.menuItemTextView.setText(mi.textResource)
        }

        return view
    }

}
