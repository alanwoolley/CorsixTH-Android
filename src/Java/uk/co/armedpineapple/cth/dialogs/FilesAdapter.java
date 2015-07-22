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
    private final FilesClickListener listener;

    public FilesAdapter(List<FileDetails> items, FilesClickListener listener ) {
        this.items = items;
        this.listener = listener;
    }


    @Override
    public FilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_card, parent, false);
        FilesViewHolder vh = new FilesViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(FilesViewHolder holder, int position) {

        final FileDetails details = items.get(position);
        holder.name.setText(details.getFileName().replace(".sav",""));
        holder.level.setText("Level " + position);
        holder.money.setText("Money: $2323453");
        holder.rep.setText("Rep: 542");
        holder.details = details;

        holder.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(details);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FilesViewHolder extends RecyclerView.ViewHolder {

        TextView rep;
        TextView money;
        TextView name;
        TextView level;
        ImageView image;
        FileDetails details;



        FilesViewHolder(View itemView) {
            super(itemView);

            rep = (TextView)itemView.findViewById(R.id.saverep);
            money = (TextView)itemView.findViewById(R.id.savemoney);
            name = (TextView)itemView.findViewById(R.id.savename);
            level = (TextView)itemView.findViewById(R.id.savelevel);
            image = (ImageView)itemView.findViewById(R.id.saveimage);


        }

        void setListener(View.OnClickListener clickListener) {
            itemView.setOnClickListener(clickListener);
        }

    }

    public interface FilesClickListener {
        void onItemClick(FileDetails details);
    }

}
