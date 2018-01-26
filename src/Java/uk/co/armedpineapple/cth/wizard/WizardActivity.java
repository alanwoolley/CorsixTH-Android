/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.wizard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ViewFlipper;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import uk.co.armedpineapple.cth.CTHActivity;
import uk.co.armedpineapple.cth.Configuration;
import uk.co.armedpineapple.cth.Configuration.ConfigurationException;
import uk.co.armedpineapple.cth.Files;
import uk.co.armedpineapple.cth.Files.StorageUnavailableException;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.Reporting;
import uk.co.armedpineapple.cth.SDLActivity;
import uk.co.armedpineapple.cth.dialogs.DialogFactory;

public class WizardActivity extends CTHActivity  implements EasyPermissions.PermissionCallbacks {

    private static final Reporting.Logger Log = Reporting.getLogger("Wizard");
    private static final int REQUIRED_PERMISSIONS = 1;
    private ViewFlipper flipper;
    private Button previousButton;
    private Button nextButton;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        bootstrap();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Log.d("User permenantly denied us permission!");
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            Log.d("User just denied us permission!");
            bootstrap();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            bootstrap();
        }
    }

    @AfterPermissionGranted(REQUIRED_PERMISSIONS)
    public void bootstrap() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            hasPermissions();
        } else {
            Log.d("Do not have permissions - requesting");
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "This application requires access to storage to run.",
                    REQUIRED_PERMISSIONS, perms);
        }
    }

    public void hasPermissions() {
        if (!Files.Companion.canAccessExternalStorage()) {
            Log.e("Can't get storage.");
            // Show dialog and end
            DialogFactory.INSTANCE.createExternalStorageWarningDialog(this, true).show();
            return;

        }

        SharedPreferences preferences = getApp().getPreferences();
        boolean alreadyRun = preferences.getBoolean("wizard_run", false);
        if (alreadyRun) {
            if (Files.Companion
                    .hasDataFiles(preferences.getString("originalfiles_pref", ""))) {
                Log.d("Wizard isn't going to run.");
                finish();
                startActivity(new Intent(this, SDLActivity.class));
                return;
            } else {
                Log.w("Configured but cannot find data files");
            }
        }

        Log.d("Wizard is going to run.");
        setContentView(R.layout.wizard);
        flipper = (ViewFlipper) findViewById(R.id.flipper);
        previousButton = (Button) findViewById(R.id.leftbutton);
        nextButton = (Button) findViewById(R.id.rightbutton);

        if (getApp().getConfiguration() == null) {
            try {
                getApp().setConfiguration(
                        Configuration.loadFromPreferences(this, preferences));
            } catch (StorageUnavailableException e) {
                Log.e("Can't get storage.");

                // Show dialog and end
                DialogFactory.INSTANCE
                        .createExternalStorageWarningDialog(this, true).show();
                return;
            }
        }

        // Add all the wizard views

        LayoutInflater inflater = getLayoutInflater();
        loadAndAdd(inflater, flipper,
                (WizardView) inflater.inflate(R.layout.wizard_welcome, null));
        loadAndAdd(inflater, flipper,
                (LanguageWizard) inflater.inflate(R.layout.wizard_language, null));
        loadAndAdd(inflater, flipper, (OriginalFilesWizard) inflater.inflate(
                R.layout.wizard_originalfiles, null));

        // Setup Buttons
        previousButton.setVisibility(View.GONE);
        WizardButtonClickListener buttonClickListener = new WizardButtonClickListener();

        previousButton.setOnClickListener(buttonClickListener);
        nextButton.setOnClickListener(buttonClickListener);

    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bootstrap();
    }

    public WizardView loadAndAdd(LayoutInflater inflater, ViewFlipper flipper,
                                 WizardView wv) {

        flipper.addView(wv);
        wv.loadConfiguration(getApp().getConfiguration());
        return wv;
    }

    private class WizardButtonClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.equals(previousButton)) {
                flipper.setInAnimation(WizardActivity.this,
                        R.animator.wizard_anim_slideinright);
                flipper.setOutAnimation(WizardActivity.this,
                        R.animator.wizard_anim_slideoutright);
                flipper.showPrevious();
            } else if (v.equals(nextButton)) {
                try {
                    ((WizardView) flipper.getCurrentView())
                            .saveConfiguration(getApp().getConfiguration());

                    if (!hasNext(flipper)) {
                        getApp().getConfiguration().saveToPreferences();

                        finish();
                        WizardActivity.this.startActivity(new Intent(WizardActivity.this,
                                SDLActivity.class));
                    } else {

                        flipper.setInAnimation(WizardActivity.this,
                                R.animator.wizard_anim_slideinleft);
                        flipper.setOutAnimation(WizardActivity.this,
                                R.animator.wizard_anim_slideoutleft);
                        flipper.showNext();
                    }

                } catch (ConfigurationException e) {
                    // Couldn't save the configuration. Don't change the view.
                    Log.w(e.getMessage());
                    return;
                }

            }

            if (hasNext(flipper)) {
                nextButton.setText(R.string.nextButton);
            } else {
                nextButton.setText(R.string.play_button);
            }
            if (hasPrevious(flipper)) {
                previousButton.setVisibility(View.VISIBLE);
            } else {
                previousButton.setVisibility(View.GONE);
            }

        }

        boolean hasNext(ViewFlipper flipper) {
            return flipper.indexOfChild(flipper.getCurrentView()) != flipper
                    .getChildCount() - 1;
        }

        boolean hasPrevious(ViewFlipper flipper) {
            return flipper.indexOfChild(flipper.getCurrentView()) != 0;
        }

    }

}
