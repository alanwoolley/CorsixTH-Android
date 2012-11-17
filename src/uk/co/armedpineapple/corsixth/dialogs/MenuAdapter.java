package uk.co.armedpineapple.corsixth.dialogs;

import java.util.List;

import uk.co.armedpineapple.corsixth.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter {

	Context context;
	List<MenuItems> items;

	public MenuAdapter(Context context, List<MenuItems> items) {
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

		if (mi.imageResource != null) {
			ImageView iv = (ImageView) view.findViewById(R.id.menu_item_image);
			iv.setImageResource(mi.imageResource);
		}

		return view;
	}

}
