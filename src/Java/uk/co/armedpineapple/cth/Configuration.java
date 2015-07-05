/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import uk.co.armedpineapple.cth.Files.StorageUnavailableException;

@SuppressWarnings("nls")
public class Configuration {

    private static Reporting.Logger Log = Reporting.getLogger("Config");

    public final static  int    RESOLUTION_DEFAULT   = 1;
    public final static  int    RESOLUTION_NATIVE    = 2;
    public final static  int    RESOLUTION_CUSTOM    = 3;
    public final static  int    CONTROLS_NORMAL      = 1;
    public final static  int    CONTROLS_DESKTOP     = 2;
    public final static  int    CONTROLS_TOUCHPAD    = 3;
    // Defaults
    public final static  int    MINIMUM_WIDTH        = 640;
    public final static  int    MINIMUM_HEIGHT       = 480;
    public final static  String DEFAULT_UNICODE_PATH = "/system/fonts/NotoSerif-Regular.ttf";
    public final static  String HEADER               = "---- CorsixTH configuration file ----------------------------------------------\n"
            + "-- Lines starting with two dashes (like this one) are ignored.\n"
            + "-- Text settings should have their values between double square braces, e.g.\n"
            + "--  setting = [[value]]\n"
            + "-- Number settings should not have anything around their value, e.g.\n"
            + "--  setting = 42\n\n\n"
            + "---- If you wish to add any custom settings, please do so below. ---- \n\n";
    public final static  String SEPARATOR            = "\n\n---- Do not edit below this line ----\n\n";
    // TODO - do this properly
    private String originalFilesPath, cthPath, language;
    private boolean globalAudio, playMusic, playAnnouncements,
            playSoundFx, keepScreenOn, debug, edgeScroll, adviser, playMovies,
            playIntroMovie, spen, autoWageGrant, haptic, hapticEarthquakes;
    private int musicVol, announcementsVol, sfxVol,
            resolutionMode, displayWidth, displayHeight, gameSpeed, fpsLimit,
            edgeBordersSize, edgeScrollSpeed, controlsMode, nativeWidth,
            nativeHeight;
    private String saveGamesPath = Files.getExtStoragePath()
            + "CTHsaves";
    private final Context           ctx;
    private final SharedPreferences preferences;

    private Configuration(Context ctx, SharedPreferences prefs) {
        this.ctx = ctx;
        this.preferences = prefs;

        // Get the device's screen dimensions
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(dm);

        // Assume that in landscape, the width is always larger than the height.
        // Sometimes this is detected the other way round.
        // TODO - find a better way to do this

        nativeWidth = Math.max(dm.widthPixels, dm.heightPixels);
        nativeHeight = Math.min(dm.widthPixels, dm.heightPixels);

    }

    /**
     * Constructs a configuration object from a SharedPreferences object
     *
     * @param ctx         a valid activityContext used to retrieve preferences
     * @param preferences the preferences object to retrieve from
     * @return a Configuration object containing the preferences
     * @throws StorageUnavailableException if storage is unavailable
     */
    public static Configuration loadFromPreferences(Context ctx,
                                                    SharedPreferences preferences) throws StorageUnavailableException {
        Configuration config = new Configuration(ctx, preferences);
        Log.d( "Loading configuration");

        config.refresh();
        config.gameSpeed = 0;

        return config;
    }

    /**
     * Saves the configuration to a SharedPreferences object
     */
    public void saveToPreferences() {
        Log.d("Saving Configuration");
        Editor editor = preferences.edit();
        editor.putString("originalfiles_pref", originalFilesPath);
        editor.putString("gamescripts_pref", cthPath);
        editor.putBoolean("audio_pref", globalAudio);
        editor.putBoolean("music_pref", playMusic);
        editor.putBoolean("announcer_pref", playAnnouncements);
        editor.putBoolean("fx_pref", playSoundFx);
        editor.putString("fxvolume_pref", String.valueOf(sfxVol));
        editor.putString("announcervolume_pref", String.valueOf(announcementsVol));
        editor.putString("musicvolume_pref", String.valueOf(musicVol));
        editor.putString("language_pref", language);
        editor.putString("resolution_pref", String.valueOf(resolutionMode));
        editor.putString("reswidth_pref", String.valueOf(displayWidth));
        editor.putString("resheight_pref", String.valueOf(displayHeight));
        editor.putBoolean("debug_pref", debug);
        editor.putBoolean("wizard_run", true);
        editor.putBoolean("screenon_pref", keepScreenOn);
        editor.putBoolean("edgescroll_pref", edgeScroll);
        editor.putString("edgebordersize_pref", String.valueOf(edgeBordersSize));
        editor.putString("edgescrollspeed_pref", String.valueOf(edgeScrollSpeed));
        editor.putBoolean("adviser_pref", adviser);
        editor.putString("fpslimit_pref", String.valueOf(fpsLimit));
        editor.putBoolean("movies_pref", playMovies);
        editor.putBoolean("intromovie_pref", playIntroMovie);
        editor.putBoolean("spen_pref", spen);
        editor.putString("controlsmode_pref", String.valueOf(controlsMode));
        editor.putBoolean("autowage_pref", autoWageGrant);

        editor.apply();

    }

    /**
     * Refresh the configuration with values from the preferences it was
     * initialised from. Does not reset any configuration options that are not
     * present in the preferences
     */
    public void refresh() {
        originalFilesPath = preferences.getString("originalfiles_pref", "");

        // TODO - No check for external storage availability
        cthPath = preferences.getString("gamescripts_pref", ctx
                .getExternalFilesDir(null).getAbsolutePath());

        globalAudio = preferences.getBoolean("audio_pref", true);
        playMusic = preferences.getBoolean("music_pref", false);
        playAnnouncements = preferences.getBoolean("announcer_pref", true);
        playSoundFx = preferences.getBoolean("fx_pref", true);
        sfxVol = Integer.valueOf(preferences.getString("fxvolume_pref", "5"));
        announcementsVol = Integer.valueOf(preferences.getString(
                "announcervolume_pref", "5"));
        musicVol = Integer.valueOf(preferences.getString("musicvolume_pref", "5"));

        language = preferences.getString("language_pref", "en");

        resolutionMode = Integer.valueOf(preferences.getString("resolution_pref",
                "1"));

        debug = preferences.getBoolean("debug_pref", false);
        adviser = preferences.getBoolean("adviser_pref", true);

        keepScreenOn = preferences.getBoolean("screenon_pref", true);

        // Edge Scrolling
        edgeScroll = preferences.getBoolean("edgescroll_pref", false);
        edgeBordersSize = Integer.valueOf(preferences.getString(
                "edgebordersize_pref", "20"));
        edgeScrollSpeed = Integer.valueOf(preferences.getString(
                "edgescrollspeed_pref", "15"));

        // Movies
        playMovies = preferences.getBoolean("movies_pref", false);
        playIntroMovie = preferences.getBoolean("intromovie_pref", true);

        // Controls
        spen = preferences.getBoolean("spen_pref", false);
        controlsMode = Integer.valueOf(preferences.getString("controlsmode_pref",
                "1"));

        autoWageGrant = preferences.getBoolean("autowage_pref", false);

        // Haptic feedback
        haptic = preferences.getBoolean("haptic_pref", false);
        hapticEarthquakes = preferences.getBoolean("earthquake_pref", false);

        if (preferences.getString("fpslimit_pref", "20").equals(
                ctx.getString(R.string.off))) {
            fpsLimit = 0;
        } else {
            fpsLimit = Integer.valueOf(preferences.getString("fpslimit_pref", "20"));
        }

		/*
         * If the resolution is default, set the resolution to 640x480.
		 *
		 * TODO - make this external
		 */

        switch (resolutionMode) {
            case RESOLUTION_DEFAULT:
                // Find the lowest possible resolution that is greater or equal to
                // 640x480 and retains the device's aspect ratio

                float ratio = Math.max(((float) MINIMUM_HEIGHT / nativeHeight),
                        ((float) MINIMUM_WIDTH / nativeWidth));

                displayWidth = (int) (nativeWidth * ratio);
                displayHeight = (int) (nativeHeight * ratio);
                Log.d("Adjusted resolution is: " + displayWidth + " x "
                        + displayHeight);
                break;

            case RESOLUTION_NATIVE:
                displayWidth = nativeWidth;
                displayHeight = nativeHeight;
                break;

            case RESOLUTION_CUSTOM:
                displayWidth = Integer.valueOf(preferences.getString("reswidth_pref",
                        String.valueOf(MINIMUM_WIDTH)).trim());
                displayHeight = Integer.valueOf(preferences.getString("resheight_pref",
                        String.valueOf(MINIMUM_HEIGHT)).trim());
                break;

        }
    }

    /**
     * Writes the configuration to the config.txt file which is read by the game
     * engine
     *
     * @throws IOException if the configuration file cannot be written to
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

            while ((reader.read(buf)) != -1) {
                b.append(buf);
            }

            String original = b.toString();

            split = original.split(SEPARATOR);

            reader.close();
        } catch (IOException e) {
            Reporting.report(e);
            Log.d("Couldn't read config file.");
        }

        StringBuilder sbuilder = new StringBuilder();
        if (split != null && split.length > 1 && split[0].length() > 0) {
            sbuilder.append(split[0]);
        } else {
            sbuilder.append(HEADER);
        }

        sbuilder.append(SEPARATOR);
        sbuilder.append("theme_hospital_install = [[").append(originalFilesPath).append("]]\n");

        sbuilder.append("prevent_edge_scrolling = ").append(String.valueOf(!edgeScroll)).append("\n");

        sbuilder.append("audio = ").append(String.valueOf(globalAudio)).append("\n");

        sbuilder.append("audio_frequency = 22050\n");
        sbuilder.append("audio_channels = 2\n");
        sbuilder.append("audio_buffer_size = 2048\n");

        sbuilder.append("play_music = ").append(String.valueOf(playMusic)).append("\n");
        sbuilder.append("music_volume = 0.").append(String.valueOf(musicVol)).append("\n");

        sbuilder.append("play_announcements = ").append(String.valueOf(playAnnouncements)).append("\n");
        sbuilder.append("announcement_volume = 0.").append(String.valueOf(announcementsVol)).append("\n");

        sbuilder.append("play_sounds = ").append(String.valueOf(playSoundFx)).append("\n");
        sbuilder.append("sound_volume = 0.").append(String.valueOf(sfxVol)).append("\n");

        sbuilder.append("language = [[").append(language).append("]]\n");

        sbuilder.append("width = ").append(String.valueOf(displayWidth)).append("\n");
        sbuilder.append("height = ").append(String.valueOf(displayHeight)).append("\n");
        sbuilder.append("fullscreen = true\n");

        sbuilder.append("debug = ").append(String.valueOf(debug)).append("\n");
        sbuilder.append("track_fps = false\n");

        File fallbackFont = new File(ctx.getFilesDir() + File.separator + "DroidSansFallbackFull.ttf");
        if (fallbackFont.exists()) {
            sbuilder.append("unicode_font = [[" + fallbackFont.getAbsolutePath() + "]]\n");
        } else {
            sbuilder.append("unicode_font = [[" + DEFAULT_UNICODE_PATH + "]]\n");

            Log.w("Couldn't find fallback font");
        }

        sbuilder.append("savegames = [[").append(saveGamesPath).append("]]\n");

        sbuilder.append("free_build_mode = false\n");
        sbuilder.append("adviser_disabled = ").append(String.valueOf(!adviser)).append("\n");
        sbuilder.append("warmth_colors_display_default = 1\n");
        sbuilder.append("allow_user_actions_while_paused = false\n");
        sbuilder.append("volume_opens_casebook = false\n");
        sbuilder.append("twentyfour_hour_clock = false\n");

        sbuilder.append("check_for_updates = false\n");
        sbuilder.append("enable_avg_contents = false\n");

        sbuilder.append("grant_wage_increase = ").append(String.valueOf(autoWageGrant)).append("\n");

        sbuilder.append("disable_fractured_bones_females = true\n");

        // Movies
        sbuilder.append("movies = ").append(String.valueOf(playMovies)).append("\n");
        sbuilder.append("play_intro = ").append(String.valueOf(playIntroMovie)).append("\n");

        // Controls
        sbuilder.append("scroll_region_size = ").append(String.valueOf(edgeBordersSize)).append("\n");
        sbuilder.append("scroll_speed = ").append(String.valueOf(edgeScrollSpeed)).append("\n");
        sbuilder.append("controls_mode = ").append(String.valueOf(controlsMode)).append("\n");
        sbuilder.append("scrolling_momentum = 0.9\n");
        sbuilder.append("zoom_speed = 80\n");

        // Aliens
        sbuilder.append("alien_dna_only_by_emergency = true\n");
        sbuilder.append("alien_dna_must_stand = true\n");
        sbuilder.append("alien_dna_can_knock_on_doors = false\n");

        FileWriter writer = new FileWriter(configFileName, false);
        writer.write(sbuilder.toString());
        writer.close();

    }

    // Getters

    public boolean getKeepScreenOn() {
        return keepScreenOn;
    }

    public void setKeepScreenOn(boolean keepScreenOn) {
        this.keepScreenOn = keepScreenOn;
    }

    public String getOriginalFilesPath() {
        return originalFilesPath;
    }

    public void setOriginalFilesPath(String originalFilesPath) {
        this.originalFilesPath = originalFilesPath;
    }

    public String getCthPath() {
        return cthPath;
    }

    public void setCthPath(String cthPath) {
        this.cthPath = cthPath;
    }

    public boolean getGlobalAudio() {
        return globalAudio;
    }

    public void setGlobalAudio(boolean globalAudio) {
        this.globalAudio = globalAudio;
    }

    public boolean getPlayMusic() {
        return playMusic;
    }

    public void setPlayMusic(boolean playMusic) {
        this.playMusic = playMusic;
    }

    public boolean getPlayAnnouncements() {
        return playAnnouncements;
    }

    public void setPlayAnnouncements(boolean playAnnouncements) {
        this.playAnnouncements = playAnnouncements;
    }

    public boolean getPlaySoundFx() {
        return playSoundFx;
    }

    public void setPlaySoundFx(boolean playSoundFx) {
        this.playSoundFx = playSoundFx;
    }

    public int getMusicVol() {
        return musicVol;
    }

    // Setters

    public void setMusicVol(int musicVol) {
        this.musicVol = musicVol;
    }

    public int getAnnouncementsVol() {
        return announcementsVol;
    }

    public void setAnnouncementsVol(int announcementsVol) {
        this.announcementsVol = announcementsVol;
    }

    public int getSfxVol() {
        return sfxVol;
    }

    public void setSfxVol(int sfxVol) {
        this.sfxVol = sfxVol;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getResolutionMode() {
        return resolutionMode;
    }

    public boolean getHaptic() {
        return haptic;
    }

    public boolean getHapticEarthquakes() {
        return hapticEarthquakes;
    }

    public void setResolutionMode(int resolutionMode) {

        switch (resolutionMode) {
            case RESOLUTION_NATIVE:
                DisplayMetrics dm = new DisplayMetrics();
                ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay().getMetrics(dm);
                this.displayWidth = dm.widthPixels;
                this.displayHeight = dm.heightPixels;
                break;
            case RESOLUTION_DEFAULT:
                this.displayWidth = MINIMUM_WIDTH;
                this.displayHeight = MINIMUM_HEIGHT;
        }

        this.resolutionMode = resolutionMode;

    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getAdviser() {
        return adviser;
    }

    public void setAdviser(boolean adviser) {
        this.adviser = adviser;
    }

    public boolean getEdgeScroll() {
        return edgeScroll;
    }

    public void setEdgeScroll(boolean edgeScroll) {
        this.edgeScroll = edgeScroll;
    }

    public int getEdgeBordersSize() {
        return edgeBordersSize;
    }

    public void setEdgeBordersSize(int edgeBordersSize) {
        this.edgeBordersSize = edgeBordersSize;
    }

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public int getFpsLimit() {
        return fpsLimit;
    }

    public void setFpsLimit(int fpsLimit) {
        this.fpsLimit = fpsLimit;
    }

    public String getSaveGamesPath() {
        return saveGamesPath;
    }

    public void setSaveGamesPath(String saveGamesPath) {
        this.saveGamesPath = saveGamesPath;
    }

    public int getEdgeScrollSpeed() {
        return edgeScrollSpeed;
    }

    public void setEdgeScrollSpeed(int edgeScrollSpeed) {
        this.edgeScrollSpeed = edgeScrollSpeed;
    }

    public Boolean getSpen() {
        return spen;
    }

    public void setSpen(Boolean spen) {
        this.spen = spen;
    }

    public int getControlsMode() {
        return controlsMode;
    }

    public void setControlsMode(int controlsMode) {
        this.controlsMode = controlsMode;
    }

    @Override
    public String toString() {
        return "Configuration [origFiles=" + originalFilesPath + ", cthPath="
                + cthPath + ", Audio?=" + globalAudio + ", Music?=" + playMusic
                + ", Announcements?=" + playAnnouncements + ", SFX?=" + playSoundFx
                + ", mVol=" + musicVol + ", aVol=" + announcementsVol + ", sfxVol="
                + sfxVol + ", language=" + language + ", resMode=" + resolutionMode
                + ", width=" + displayWidth + ", height=" + displayHeight + ", debug?="
                + debug + "]";
    }

    public static class ConfigurationException extends Exception {

        private static final long serialVersionUID = 2599028839849130837L;

    }

}
