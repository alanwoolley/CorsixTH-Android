/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard;

import java.util.Locale;

import uk.co.armedpineapple.cth.Configuration;
import uk.co.armedpineapple.cth.ConfigurationException;
import uk.co.armedpineapple.cth.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class LanguageWizard extends WizardView {
	Context		ctx;
	ListView	languageListView;
	
	private static final String	LOG_TAG	= "LanguageWizard";
	
	public LanguageWizard(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (!isInEditMode()) {
			languageListView = (ListView) findViewById(R.id.language_list);
			String[] langArray = getResources().getStringArray(R.array.languages);
			String[] langValuesArray = getResources().getStringArray(
					R.array.languages_values);
			TypedArray langFlagsArray = getResources().obtainTypedArray(
					R.array.languages_flags);
			languageListView.setAdapter(new LanguageListAdapter(ctx, langArray,
					langValuesArray, langFlagsArray));

			languageListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
						long arg3) {
					((LanguageListAdapter) arg0.getAdapter()).setSelectedItem(pos);

				}
			});
			// Look for the language in the values array
			Log.d(LOG_TAG, "System Language: "
					+ Locale.getDefault().getLanguage());

			for (int i = 0; i < langValuesArray.length; i++) {
				if (langValuesArray[i].equals(Locale.getDefault().getLanguage())) {
					((LanguageListAdapter) languageListView.getAdapter())
							.setSelectedItem(i);
					break;
				}

			}
		}
	}

	@Override
	void saveConfiguration(Configuration config) throws ConfigurationException {

		String lang = (String) ((LanguageListAdapter) languageListView.getAdapter())
				.getSelectedItem();

		config.setLanguage(lang);

	}

	@Override
	void loadConfiguration(Configuration config) {

	}

	class LanguageListAdapter extends BaseAdapter {
		String[]				text;
		String[]				values;
		TypedArray			resources;

		LayoutInflater	inflater;
		int							selected	= 0;

		public LanguageListAdapter(Context ctx, String[] langArray,
				String[] langValuesArray, TypedArray langFlagsArray) {
			super();
			text = langArray;
			values = langValuesArray;
			resources = langFlagsArray;
			inflater = LayoutInflater.from(ctx);

		}

		@Override
		public int getCount() {
			return text.length;
		}

		@Override
		public Object getItem(int position) {
			return values[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public Object getSelectedItem() {
			return getItem(selected);
		}

		public void setSelectedItem(int pos) {
			selected = pos;
			notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View newView;

			if (convertView == null) {
				newView = inflater.inflate(R.layout.language_list_item, null);
			} else {
				newView = convertView;
			}

			((ImageView) newView.findViewById(R.id.language_flag))
					.setImageDrawable(resources.getDrawable(position));
			((TextView) newView.findViewById(R.id.language_text))
					.setText(text[position]);

			if (selected == position) {
				((ImageView) newView.findViewById(R.id.language_tick))
						.setVisibility(VISIBLE);
			} else {
				((ImageView) newView.findViewById(R.id.language_tick))
						.setVisibility(INVISIBLE);
			}

			return newView;
		}
	}

}
