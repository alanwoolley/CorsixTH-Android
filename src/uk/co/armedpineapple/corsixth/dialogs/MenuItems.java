package uk.co.armedpineapple.corsixth.dialogs;

import java.util.Arrays;
import java.util.List;

import uk.co.armedpineapple.corsixth.R;
import uk.co.armedpineapple.corsixth.R.drawable;

public enum MenuItems {
	GAME_SPEED("Game Speed", null), QUICK_LOAD("Quick Load", null), QUICK_SAVE(
			"Quick Save", null), LOAD("Load", R.drawable.ic_menu_load), SAVE(
			"Save", R.drawable.ic_menu_save), RESTART("Restart Level",
			R.drawable.ic_menu_restart), WIZARD("Setup Wizard",
			R.drawable.ic_menu_wizard), ABOUT("About", R.drawable.ic_menu_help), EXIT(
			"Exit Game", R.drawable.ic_menu_exit);

	public Integer imageResource;
	public String text;

	MenuItems(String text, Integer imageResource) {
		this.imageResource = imageResource;
		this.text = text;
	}

	public static List<MenuItems> getItems() {
		return Arrays.asList(MenuItems.values());
	}

}