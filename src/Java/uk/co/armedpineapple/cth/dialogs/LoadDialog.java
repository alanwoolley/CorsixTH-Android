/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import java.io.File;
import java.io.IOException;

import uk.co.armedpineapple.cth.CommandHandler.Command;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Reporting;
import uk.co.armedpineapple.cth.SDLActivity;

public class LoadDialog extends FilesDialog {

    private static final Reporting.Logger Log  = Reporting.getLogger("LoadDialog");
    private static final String AUTOSAVES = "autosaves";
    private final LinearLayout tabsView;
    private final TabHost      tabHost;
    private       RecyclerView userSavesList;
    private       RecyclerView     autoSavesList;

    public LoadDialog(SDLActivity context, String path) {
        super(context, path, R.layout.files_dialog, R.string.load_game_dialog_title);

        FrameLayout flayout = (FrameLayout) findViewById(R.id.files_frame);
        tabsView = (LinearLayout) getLayoutInflater().inflate(R.layout.files_load_tabs, null);
        flayout.addView(tabsView);

        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabSpec userSavesSpec = tabHost.newTabSpec("Saves");
        userSavesSpec.setIndicator("Saves");
        userSavesSpec.setContent(R.id.user_files);
        TabSpec autoSavesSpec = tabHost.newTabSpec("Autosaves");
        autoSavesSpec.setIndicator("Autosaves");
        autoSavesSpec.setContent(R.id.autosave_files);

        tabHost.addTab(userSavesSpec);
        tabHost.addTab(autoSavesSpec);
        tabHost.setCurrentTab(0);

        userSavesList = (RecyclerView) findViewById(R.id.user_files);
        autoSavesList = (RecyclerView) findViewById(R.id.autosave_files);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        userSavesList.setLayoutManager(llm);

        LinearLayoutManager llmb = new LinearLayoutManager(context);
        autoSavesList.setLayoutManager(llmb);


        findViewById(R.id.fab).setVisibility(View.GONE);
    }

    @Override
    public void onSelectedFile(String directory, String file) {
        SDLActivity.sendCommand(Command.HIDE_MENU, null);

        Log.d("Loading: " + file);
        if (directory.endsWith(File.separator + AUTOSAVES)) {
            SDLActivity.cthLoadGame(AUTOSAVES + File.separator + file);
        }
        SDLActivity.cthLoadGame(file);


        dismiss();
    }

    @Override
    public void refreshSaves(Context ctx) throws IOException {
        updateSaves(ctx, userSavesList, path);
        updateSaves(ctx, autoSavesList, path + File.separator + AUTOSAVES);
    }


}
