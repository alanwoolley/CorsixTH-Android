package uk.co.armedpineapple.corsixth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class Configuration {

	public final static int RESOLUTION_DEFAULT = 1;
	public final static int RESOLUTION_NATIVE = 2;
	public final static int RESOLUTION_CUSTOM = 3;

	public final static String HEADER = "---- CorsixTH configuration file ----------------------------------------------\n"
			+ "-- Lines starting with two dashes (like this one) are ignored.\n"
			+ "-- Text settings should have their values between double square braces, e.g.\n"
			+ "--  setting = [[value]]\n"
			+ "-- Number settings should not have anything around their value, e.g.\n"
			+ "--  setting = 42\n\n\n"
			+ "---- If you wish to add any custom settings, please do so below. ---- \n\n";

	public final static String SEPARATOR = "\n\n---- Do not edit below this line ----\n\n";

	public final static String UNICODE_PATH = "/system/fonts/DroidSansFallback.ttf";

	private String originalFilesPath, cthPath, language;
	private Boolean globalAudio, playMusic, playAnnouncements, playSoundFx,
			keepScreenOn, debug;

	private Integer musicVol, announcementsVol, sfxVol, resolutionMode,
			displayWidth, displayHeight;
	private String saveGamesPath = "/sdcard/CTHsaves";

	private Configuration() {
	}

	/** Saves the configuration to a SharedPreferences object **/
	public void saveToPreferences(Context ctx, SharedPreferences preferences) {
		Editor editor = preferences.edit();
		editor.putString("originalfiles_pref", originalFilesPath);
		editor.putString("gamescripts_pref", cthPath);
		editor.putBoolean("audio_pref", globalAudio);
		editor.putBoolean("music_pref", playMusic);
		editor.putBoolean("announcer_pref", playAnnouncements);
		editor.putBoolean("fx_pref", playSoundFx);
		editor.putString("fxvolume_pref", String.valueOf(sfxVol));
		editor.putString("announcervolume_pref",
				String.valueOf(announcementsVol));
		editor.putString("musicvolume_pref", String.valueOf(musicVol));
		editor.putString("language_pref", language);
		editor.putString("resolution_pref", String.valueOf(resolutionMode));
		editor.putString("reswidth_pref", String.valueOf(displayWidth));
		editor.putString("resheight_pref", String.valueOf(displayHeight));
		editor.putBoolean("debug_pref", debug);
		editor.putBoolean("wizard_run", true);
		editor.putBoolean("screenon_pref", keepScreenOn);
		editor.commit();

	}

	/** Load the configuration from a SharedPreferences object */
	public static Configuration loadFromPreferences(Context ctx,
			SharedPreferences preferences) {
		Configuration config = new Configuration();
		Log.d(Configuration.class.getSimpleName(), "Loading configuration");

		config.originalFilesPath = preferences.getString("originalfiles_pref",
				"");

		config.cthPath = preferences.getString("gamescripts_pref", ctx
				.getExternalFilesDir(null).getAbsolutePath());

		config.globalAudio = preferences.getBoolean("audio_pref", true);
		config.playMusic = preferences.getBoolean("music_pref", false);
		config.playAnnouncements = preferences.getBoolean("announcer_pref",
				true);
		config.playSoundFx = preferences.getBoolean("fx_pref", true);
		config.sfxVol = Integer.valueOf(preferences.getString("fxvolume_pref",
				"5"));
		config.announcementsVol = Integer.valueOf(preferences.getString(
				"announcervolume_pref", "5"));
		config.musicVol = Integer.valueOf(preferences.getString(
				"musicvolume_pref", "5"));

		config.language = preferences.getString("language_pref", "en");

		config.resolutionMode = Integer.valueOf(preferences.getString(
				"resolution_pref", "1"));

		config.debug = preferences.getBoolean("debug_pref", false);

		config.keepScreenOn = preferences.getBoolean("screenon_pref", true);

		/*
		 * If the resolution is default, set the resolution to 640x480.
		 * 
		 * TODO - make this external
		 */

		switch (config.resolutionMode) {
		case RESOLUTION_DEFAULT:
			config.displayWidth = 640;
			config.displayHeight = 480;
			break;

		/*
		 * TODO - the native resolution is easy to get but can sometimes be
		 * misleading because of the buttons on Android 3.0+. There's probably a
		 * much better way of doing this
		 */

		case RESOLUTION_NATIVE:
			DisplayMetrics dm = new DisplayMetrics();
			((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay().getMetrics(dm);
			config.displayWidth = dm.widthPixels;
			config.displayHeight = dm.heightPixels;
			break;

		case RESOLUTION_CUSTOM:
			config.displayWidth = Integer.valueOf(preferences.getString(
					"reswidth_pref", "640"));
			config.displayHeight = Integer.valueOf(preferences.getString(
					"resheight_pref", "480"));
			break;

		}
		Log.d(Configuration.class.getSimpleName(), config.toString());
		return config;
	}

	/**
	 * Writes the configuration to the config.txt file which is read by the game
	 */
	public void writeToFile() throws IOException {
		String configFileName = cthPath + "/scripts/" + "config.txt";

		File file = new File(configFileName);
		file.getParentFile().mkdirs();
		String[] split = null;
		try {
			FileReader reader = new FileReader(file);

			StringBuilder b = new StringBuilder();
			char[] buf = new char[1024];
			int read;

			while ((read = reader.read(buf)) != -1) {
				b.append(buf);
			}

			String original = b.toString();

			split = original.split(SEPARATOR);

			reader.close();
		} catch (IOException e) {
			Log.d(getClass().getSimpleName(), "Couldn't read config file.");
		}

		StringBuilder sbuilder = new StringBuilder();
		if (split != null && split.length > 1 && split[0].length() > 0) {
			sbuilder.append(split[0]);
		} else {
			sbuilder.append(HEADER);
		}

		sbuilder.append(SEPARATOR);
		sbuilder.append("theme_hospital_install = [[" + originalFilesPath
				+ "]]\n");
		sbuilder.append("prevent_edge_scrolling = true\n");

		sbuilder.append("audio = " + String.valueOf(globalAudio) + "\n");

		sbuilder.append("audio_frequency = 22050\n");
		sbuilder.append("audio_channels = 2\n");
		sbuilder.append("audio_buffer_size = 2048\n");

		sbuilder.append("play_music = " + String.valueOf(playMusic) + "\n");
		sbuilder.append("music_volume = 0." + String.valueOf(musicVol) + "\n");

		sbuilder.append("play_announcements = "
				+ String.valueOf(playAnnouncements) + "\n");
		sbuilder.append("announcement_volume = 0."
				+ String.valueOf(announcementsVol) + "\n");

		sbuilder.append("play_sounds = " + String.valueOf(playSoundFx) + "\n");
		sbuilder.append("sound_volume = 0." + String.valueOf(sfxVol) + "\n");

		sbuilder.append("language = [[" + language + "]]\n");

		sbuilder.append("width = " + String.valueOf(displayWidth) + "\n");
		sbuilder.append("height = " + String.valueOf(displayHeight) + "\n");
		sbuilder.append("fullscreen = true\n");

		sbuilder.append("debug = " + String.valueOf(debug) + "\n");
		sbuilder.append("track_fps = false\n");
		sbuilder.append("unicode_font = [[" + UNICODE_PATH + "]]\n");
		sbuilder.append("savegames = [[" + saveGamesPath + "]]");

		// Create all the directories leading up to the config.txt file

		FileWriter writer = new FileWriter(configFileName, false);
		writer.write(sbuilder.toString());
		writer.close();

	}

	// Getters

	public Boolean getKeepScreenOn() {
		return keepScreenOn;
	}

	public String getOriginalFilesPath() {
		return originalFilesPath;
	}

	public String getCthPath() {
		return cthPath;
	}

	public Boolean getGlobalAudio() {
		return globalAudio;
	}

	public Boolean getPlayMusic() {
		return playMusic;
	}

	public Boolean getPlayAnnouncements() {
		return playAnnouncements;
	}

	public Boolean getPlaySoundFx() {
		return playSoundFx;
	}

	public Integer getMusicVol() {
		return musicVol;
	}

	public Integer getAnnouncementsVol() {
		return announcementsVol;
	}

	public Integer getSfxVol() {
		return sfxVol;
	}

	public String getLanguage() {
		return language;
	}

	public Integer getResolutionMode() {
		return resolutionMode;
	}

	public Integer getDisplayWidth() {
		return displayWidth;
	}

	public Integer getDisplayHeight() {
		return displayHeight;
	}

	public Boolean getDebug() {
		return debug;
	}

	// Setters

	public void setOriginalFilesPath(String originalFilesPath) {
		this.originalFilesPath = originalFilesPath;
	}

	public void setCthPath(String cthPath) {
		this.cthPath = cthPath;
	}

	public void setGlobalAudio(Boolean globalAudio) {
		this.globalAudio = globalAudio;
	}

	public void setPlayMusic(Boolean playMusic) {
		this.playMusic = playMusic;
	}

	public void setPlayAnnouncements(Boolean playAnnouncements) {
		this.playAnnouncements = playAnnouncements;
	}

	public void setPlaySoundFx(Boolean playSoundFx) {
		this.playSoundFx = playSoundFx;
	}

	public void setMusicVol(Integer musicVol) {
		this.musicVol = musicVol;
	}

	public void setAnnouncementsVol(Integer announcementsVol) {
		this.announcementsVol = announcementsVol;
	}

	public void setSfxVol(Integer sfxVol) {
		this.sfxVol = sfxVol;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setResolutionMode(Integer resolutionMode) {
		this.resolutionMode = resolutionMode;
	}

	public void setDisplayWidth(Integer displayWidth) {
		this.displayWidth = displayWidth;
	}

	public void setDisplayHeight(Integer displayHeight) {
		this.displayHeight = displayHeight;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	public void setKeepScreenOn(Boolean keepScreenOn) {
		this.keepScreenOn = keepScreenOn;
	}

	@Override
	public String toString() {
		return "Configuration [origFiles=" + originalFilesPath + ", cthPath="
				+ cthPath + ", Audio?=" + globalAudio + ", Music?=" + playMusic
				+ ", Announcements?=" + playAnnouncements + ", SFX?="
				+ playSoundFx + ", mVol=" + musicVol + ", aVol="
				+ announcementsVol + ", sfxVol=" + sfxVol + ", language="
				+ language + ", resMode=" + resolutionMode + ", width="
				+ displayWidth + ", height=" + displayHeight + ", debug?="
				+ debug + "]";
	}

	public String getSaveGamesPath() {
		return saveGamesPath;
	}

	public void setSaveGamesPath(String saveGamesPath) {
		this.saveGamesPath = saveGamesPath;
	}

}
