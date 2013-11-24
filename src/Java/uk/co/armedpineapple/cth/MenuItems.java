package uk.co.armedpineapple.cth;

import java.util.Arrays;
import java.util.List;

import uk.co.armedpineapple.cth.R;

public enum MenuItems {
	GAME_SPEED(R.string.menuitem_change_game_speed, null),
	QUICK_LOAD(R.string.menuitem_quickload, null),
	QUICK_SAVE(R.string.menuitem_quicksave, null),
	LOAD(R.string.menuitem_load, null),
	SAVE(R.string.menuitem_save, null),
	RESTART(R.string.menuitem_restart, null),
	SETTINGS(R.string.menuitem_settings, null),
	HELP(R.string.menuitem_help, null),
	ABOUT(R.string.menuitem_about,null),
	EXIT(R.string.menuitem_exit, null);

	public Integer	imageResource, textResource;

	MenuItems(Integer textResource, Integer imageResource) {
		this.imageResource = imageResource;
		this.textResource = textResource;
	}

	public static List<MenuItems> getItems() {
		return Arrays.asList(MenuItems.values());
	}

}