package uk.co.armedpineapple.cth.dialogs;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import uk.co.armedpineapple.cth.FileDetails;
import uk.co.armedpineapple.cth.Files;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Reporting;
import uk.co.armedpineapple.cth.persistence.PersistenceHelper;
import uk.co.armedpineapple.cth.persistence.SaveData;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {

    private final List<FileDetails> items;
    private final FilesClickListener listener;
    private Dao<SaveData, String> dao;

    public FilesAdapter(List<FileDetails> items, FilesClickListener listener, PersistenceHelper persistence ) {
        this.items = items;
        this.listener = listener;

        try {
            this.dao = persistence.getDao(SaveData.class);
        } catch (SQLException e) {
           Reporting.report(e);
        }
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
        holder.details = details;

        if (holder.saveData == null) {
            String longPath = details.getDirectory() + File.separator + details.getFileName();
            try {
                SaveData saveData = dao.queryForId(longPath);
                if (saveData != null) {
                    holder.saveData = saveData;
                }
            } catch (SQLException e) {
                Reporting.report(e);
            }
        }

        if (holder.saveData != null) {
            holder.level.setVisibility(View.VISIBLE);
            holder.rep.setVisibility(View.VISIBLE);
            holder.money.setVisibility(View.VISIBLE);
            Resources res = holder.itemView.getResources();
            holder.level.setText(res.getString(R.string.save_level_text, holder.saveData.levelName));
            holder.rep.setText(res.getString(R.string.save_rep_text, holder.saveData.rep));
            holder.money.setText(res.getString(R.string.save_balance_text, holder.saveData.money));

            if (Files.doesFileExist(holder.saveData.screenshotPath)) {
                holder.image.setImageBitmap(BitmapFactory.decodeFile(holder.saveData.screenshotPath));
            } else {
                holder.image.setVisibility(View.INVISIBLE);
            }

        } else {
            holder.level.setVisibility(View.GONE);
            holder.rep.setVisibility(View.GONE);
            holder.money.setVisibility(View.GONE);
            holder.image.setVisibility(View.INVISIBLE);
        }

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
        SaveData saveData;



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
