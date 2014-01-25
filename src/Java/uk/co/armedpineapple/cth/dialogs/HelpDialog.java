package uk.co.armedpineapple.cth.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;

public class HelpDialog extends Dialog implements OnItemClickListener {

    private HelpListAdapter mHelpListAdapter;
    private ListView        mHelpListView;
    private Context         ctx;

    public HelpDialog(SDLActivity context) {
        super(context);
        this.ctx = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.help_dialog);

        mHelpListView = (ListView) findViewById(R.id.help_list);
        mHelpListAdapter = new HelpListAdapter(context, context.getResources()
                .getStringArray(R.array.help_list), context.getResources()
                .getStringArray(R.array.help_values));
        mHelpListView.setAdapter(mHelpListAdapter);
        mHelpListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterview, View arg1, int pos, long arg3) {
        String intentUrl = (String) mHelpListAdapter.getItem(pos);
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intentUrl));
		ctx.startActivity(browserIntent);
		
	}

	class HelpListAdapter extends BaseAdapter {
		String[]				text;
		String[]				urls;

		LayoutInflater	inflater;
		int							selected	= 0;

		public HelpListAdapter(Context ctx, String[] helpArray, String[] helpUrls) {
			super();
			text = helpArray;
			urls = helpUrls;
			inflater = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			return text.length;
		}

		@Override
		public Object getItem(int position) {
			return urls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View newView;

			if (convertView == null) {
				newView = inflater.inflate(R.layout.menu_item, null);
			} else {
				newView = convertView;
			}

			((TextView) newView.findViewById(R.id.menu_item_text))
					.setText(text[position]);

			return newView;
		}
	}




}
