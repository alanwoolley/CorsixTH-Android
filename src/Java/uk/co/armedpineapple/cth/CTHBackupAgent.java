package uk.co.armedpineapple.cth;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class CTHBackupAgent extends BackupAgentHelper {

	@Override
	public void onCreate() {
		addHelper("preferences", new PrefBackupHelper(getApplicationContext()));
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode,
			ParcelFileDescriptor newState) throws IOException {
		super.onRestore(data, appVersionCode, newState);

		SharedPreferences p = getSharedPreferences(
				CTHApplication.PREFERENCES_KEY, Context.MODE_PRIVATE);
		Editor editor = p.edit();
		editor.putBoolean("wizard_run", true);
		editor.putInt("last_version", 0);
		editor.commit();

	}

	class PrefBackupHelper extends SharedPreferencesBackupHelper {

		public PrefBackupHelper(Context context) {
			super(context, CTHApplication.PREFERENCES_KEY);

		}

	}

}
