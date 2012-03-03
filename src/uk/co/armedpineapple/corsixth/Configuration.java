package uk.co.armedpineapple.corsixth;

import java.io.FileWriter;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Configuration {

	private String originalFilesPath;
	private String cthPath;
	private Boolean globalAudio;
	private Boolean playMusic;
	private Boolean playAnnouncements;
	private Boolean playSoundFx;
	private Integer musicVol;
	private Integer announcementsVol;
	private Integer sfxVol;
	private String language;
	private Integer resolutionMode;
	private Integer displayWidth;
	private Integer displayHeight;
	private Boolean debug;

	private Configuration() {
	}

	public static Configuration loadFromPreferences(Context ctx,
			SharedPreferences preferences) {
		Configuration config = new Configuration();

		config.originalFilesPath = preferences.getString("originalfiles_pref",
				"");
		config.cthPath = preferences.getString("gamescripts_pref", ctx
				.getExternalFilesDir(null).getAbsolutePath());

		config.globalAudio = preferences.getBoolean("audio_pref", true);
		config.playMusic = preferences.getBoolean("music_pref", true);
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

		switch (config.resolutionMode) {
		case 1:
			// Default resolution
			config.displayWidth = 640;
			config.displayHeight = 480;
			break;
		case 2:
			// Native resolution
			DisplayMetrics dm = new DisplayMetrics();
			((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay().getMetrics(dm);
			config.displayWidth = dm.widthPixels;
			config.displayHeight = dm.heightPixels;
			break;
		case 3:
			// Custom resolution
			config.displayWidth = Integer.valueOf(preferences.getString(
					"reswidth_pref", "640"));
			config.displayWidth = Integer.valueOf(preferences.getString(
					"resheight_pref", "480"));
			break;
		}

		return config;
	}

	public void writeToFile() throws IOException {
		StringBuilder sbuilder = new StringBuilder();
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

		String configFileName = cthPath + "/scripts/" + "config.txt";

		FileWriter writer = new FileWriter(configFileName, false);
		writer.write(sbuilder.toString());
		writer.close();
	}

	// Getters
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

}
