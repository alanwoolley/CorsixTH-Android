package uk.co.armedpineapple.corsixth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogFactory {

	public static Dialog createExternalStorageDialog(final Context ctx,
			boolean finish) {

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

		builder.setMessage(
				ctx.getResources().getString(R.string.no_external_storage))
				.setCancelable(false);
		if (finish) {

			builder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							((Activity) ctx).finish();
						}

					});

		} else {
			builder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}

					});
		}

		return builder.create();
	}

	public static Dialog createFromException(Exception e, final Context ctx, boolean finish) {
		return createFromException(e, null, ctx, finish);
	}
	public static Dialog createFromException(Exception e, String title, final Context ctx,
			boolean finish) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

		builder.setCancelable(false);
		if (title == null) {
			builder.setTitle("Error!");
		} else {
			builder.setTitle(title);
		}
		builder.setMessage(e.getLocalizedMessage());

		if (finish) {
			builder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							((Activity) ctx).finish();
						}

					});
		} else {
			builder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}

					});
		}

		return builder.create();
	}
}
