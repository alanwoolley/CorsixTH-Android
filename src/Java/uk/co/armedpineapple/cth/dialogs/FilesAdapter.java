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

import uk.co.armedpineapple.cth.FileDetails;
import uk.co.armedpineapple.cth.R;

public class FilesAdapter extends BaseAdapter {

    private final Context context;
    private final List<FileDetails> items;

    public FilesAdapter(Context context, List<FileDetails> items) {
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

        if (convertView != null) {
            // If we have a view we can use already, use it
            view = convertView;
        } else {
            // Otherwise, create a new one
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.files_list_item, parent, false);
        }

        TextView largeText = (TextView) view.findViewById(R.id.saveGameLargeText);
        TextView smallText = (TextView) view.findViewById(R.id.saveGameSmallText);



        smallText.setVisibility(View.VISIBLE);

        FileDetails item = (FileDetails) getItem(position);
        String fileName = item.getFileName();

        // Show the file name, minus the extension
        largeText.setText(fileName.substring(0, fileName.length() - 4));

        smallText.setText(DateFormat.getDateFormat(context).format(
                item.getLastModified()));
        return view;
    }

}
