/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import it.gmariotti.changelibs.library.view.ChangeLogListView;
import uk.co.armedpineapple.cth.Files;
import uk.co.armedpineapple.cth.R;
import uk.co.armedpineapple.cth.SDLActivity;

public class DialogFactory {

    private DialogFactory() {

    }

    public static Dialog createTestingWarningDialog(final Context ctx, OnClickListener positive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(
                        ctx.getResources().getString(R.string.preferences_alpha_warning_text))
                .setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }

        });
        builder.setPositiveButton(R.string.ok, positive);
        builder.setTitle(R.string.warning);
        return builder.create();

    }

    public static Dialog createAboutDialog(final SDLActivity ctx) {
        final Dialog d = new Dialog(ctx);

        d.setContentView(R.layout.about);

        d.getWindow()
                .setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        d.setTitle(R.string.about_dialog_header);

        Button button = (Button) d.findViewById(R.id.dismissDialogButton);
        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                d.dismiss();
            }

        });

        d.setOnShowListener(new OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                TextView aboutText = (TextView) d.findViewById(R.id.aboutTextView);
                String text;
                try {
                    text = Files.readTextFromResource(ctx, R.raw.about);
                } catch (IOException e) {
                    text = "";
                }
                aboutText.setText(text);
            }

        });

        return d;
    }

    public static Dialog createRecentChangesDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        ChangeLogListView chgList=(ChangeLogListView)((LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.changes_dialog, null);
        builder.setView(chgList);
        builder.setTitle(R.string.recent_changes_title);


        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }
        );
        return builder.create();

    }

    public static Dialog createNoNetworkConnectionDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(
                ctx.getResources().getString(R.string.no_connection_error))
                .setCancelable(false);

        builder.setNeutralButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                });

        return builder.create();
    }

    public static Dialog createErrorDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(ctx.getResources().getString(R.string.load_error))
                .setCancelable(false);
        builder.setNeutralButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SDLActivity.nativeQuit();
                    }

                });

        return builder.create();

    }

    public static Dialog createMobileNetworkWarningDialog(final Context ctx,
                                                          OnClickListener positive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(
                ctx.getResources().getString(R.string.mobile_network_warning))
                .setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }

        });
        builder.setPositiveButton(R.string.ok, positive);
        builder.setTitle(R.string.warning);
        return builder.create();

    }

    public static Dialog createExternalStorageWarningDialog(final Context ctx,
                                                            boolean finish) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setMessage(
                ctx.getResources().getString(R.string.no_external_storage))
                .setCancelable(false);
        if (finish) {

            builder.setNeutralButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) ctx).finish();
                        }

                    });

        } else {
            builder.setNeutralButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }

                    });
        }

        return builder.create();
    }

    public static Dialog createFromException(Exception e, final Context ctx,
                                             boolean finish) {
        return createFromException(e, null, ctx, finish);
    }

    public static Dialog createFromException(Exception e, String title,
                                             final Context ctx, boolean finish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

        builder.setCancelable(false);
        if (title == null) {
            builder.setTitle(R.string.dialog_error_title);
        } else {
            builder.setTitle(title);
        }
        builder.setMessage(e.getLocalizedMessage());

        if (finish) {
            builder.setNeutralButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) ctx).finish();
                        }

                    });
        } else {
            builder.setNeutralButton(R.string.ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }

                    });
        }

        return builder.create();
    }
}
