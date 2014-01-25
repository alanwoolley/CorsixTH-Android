package uk.co.armedpineapple.cth.dialogs;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import uk.co.armedpineapple.cth.Files.FileDetails;
import uk.co.armedpineapple.cth.R;

public class FilesAdapter extends BaseAdapter {

    private final Context           context;
    private final List<FileDetails> items;
    private final boolean           newButton;

    public FilesAdapter(Context context, List<FileDetails> items,
                        boolean newbutton) {
        this.context = context;
        this.items = items;
        this.newButton = newbutton;
    }

    @Override
    public int getCount() {
        return newButton ? items.size() + 1 : items.size();
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

		if (convertView != null) {
			// If we have a view we can use already, use it
			view = convertView;
		} else {
			// Otherwise, create a new one
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.files_list, parent, false);
		}

		TextView largeText = (TextView) view.findViewById(R.id.saveGameLargeText);
		TextView smallText = (TextView) view.findViewById(R.id.saveGameSmallText);
		ImageView image = (ImageView) view.findViewById(R.id.saveGameImage);

		if (position == 0 && newButton) {

			// If it's the first item and we are to show a new save button, make
			// sure the image is shown and the small text is hidden

			image.setVisibility(View.VISIBLE);
			smallText.setVisibility(View.GONE);

			largeText.setText(R.string.new_save);

		} else {
			int actualPosition = newButton ? position - 1 : position;

			// If it's any other item, hide the image and show the small text

			image.setVisibility(View.GONE);
			smallText.setVisibility(View.VISIBLE);

			FileDetails item = (FileDetails) getItem(actualPosition);
			String fileName = item.getFileName();

			// Show the file name, minus the extension
			largeText.setText(fileName.substring(0, fileName.length() - 4));

			smallText.setText(DateFormat.getDateFormat(context).format(
					item.getLastModified()));

		}
		return view;
	}

}
