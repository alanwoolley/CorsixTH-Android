package uk.co.armedpineapple.cth.dialogs;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import uk.co.armedpineapple.cth.FileDetails;
import uk.co.armedpineapple.cth.R;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    private final List<FileDetails> items;

    public FilesAdapter(List<FileDetails> items) {
        this.items = items;
    }


    @Override
    public FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_card, parent, false);
        FilesViewHolder vh = new FilesViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(FilesViewHolder holder, int position) {

        FileDetails details = items.get(position);
        holder.name.setText(details.getFileName());
        holder.level.setText("Level " + position);
        holder.money.setText("Money: $2323453");
        holder.rep.setText("Rep: 542");

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FilesViewHolder extends RecyclerView.ViewHolder {
       // CardView cv;
        TextView rep;
        TextView money;
        TextView name;
        TextView level;
        ImageView image;


        FilesViewHolder(View itemView) {
            super(itemView);
            //cv = (CardView)itemView.findViewById(R.id.savecard);
            rep = (TextView)itemView.findViewById(R.id.saverep);
            money = (TextView)itemView.findViewById(R.id.savemoney);
            name = (TextView)itemView.findViewById(R.id.savename);
            level = (TextView)itemView.findViewById(R.id.savelevel);
            image = (ImageView)itemView.findViewById(R.id.saveimage);
        }
    }

}
