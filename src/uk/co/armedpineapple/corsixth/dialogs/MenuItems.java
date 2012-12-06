package uk.co.armedpineapple.corsixth.dialogs;

import java.util.Arrays;
import java.util.List;

import uk.co.armedpineapple.corsixth.R;

public enum MenuItems {
	GAME_SPEED(R.string.menuitem_change_game_speed, null),
	QUICK_LOAD(R.string.menuitem_quickload, null),
	QUICK_SAVE(R.string.menuitem_quicksave, null),
	LOAD(R.string.menuitem_load, R.drawable.ic_menu_load),
	SAVE(R.string.menuitem_save, R.drawable.ic_menu_save),
	RESTART(R.string.menuitem_restart, R.drawable.ic_menu_restart),
	WIZARD(R.string.setup_wizard, R.drawable.ic_menu_wizard),
	ABOUT(R.string.menuitem_about, R.drawable.ic_menu_help),
	EXIT(R.string.menuitem_exit, R.drawable.ic_menu_exit);

	public Integer	imageResource, textResource;

	MenuItems(Integer textResource, Integer imageResource) {
		this.imageResource = imageResource;
		this.textResource = textResource;
	}

	public static List<MenuItems> getItems() {
		return Arrays.asList(MenuItems.values());
	}

}