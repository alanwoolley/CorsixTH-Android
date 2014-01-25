package uk.co.armedpineapple.cth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class NavDrawerAdapter extends BaseAdapter {

    final Context         context;
    final List<MenuItems> items;

    public NavDrawerAdapter(Context context, List<MenuItems> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view;
		MenuItems mi = (MenuItems) getItem(position);

		if (convertView != null) {
			view = convertView;
		} else {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.menu_item, parent, false);
		}
		TextView tv = (TextView) view.findViewById(R.id.menu_item_text);
		tv.setText(mi.textResource);

		return view;
	}

}
