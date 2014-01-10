package uk.co.armedpineapple.cth;

import java.util.ArrayList;
import java.util.List;

public enum MenuItems {
    GAME_SPEED(R.string.menuitem_change_game_speed, null, false),
    QUICK_LOAD(R.string.menuitem_quickload, null, false),
    QUICK_SAVE(R.string.menuitem_quicksave, null, false),
    LOAD(R.string.menuitem_load, null, false),
    SAVE(R.string.menuitem_save, null, false),
    RESTART(R.string.menuitem_restart, null, false),
    SETTINGS(R.string.menuitem_settings, null, false),
    CHEATS(R.string.menuitem_cheats, null, true),
    HELP(R.string.menuitem_help, null, false),
    ABOUT(R.string.menuitem_about, null, false),
    EXIT(R.string.menuitem_exit, null, false);

    public Integer imageResource, textResource;
    private boolean debugOnly;

    MenuItems(Integer textResource, Integer imageResource, boolean debugOnly) {
        this.imageResource = imageResource;
        this.textResource = textResource;
        this.debugOnly = debugOnly;
    }

    public static List<MenuItems> getItems(boolean inDebugMode) {
        ArrayList<MenuItems> items = new ArrayList<MenuItems>();

        for (MenuItems item : MenuItems.values()) {
            if (!item.debugOnly || item.debugOnly == inDebugMode)
                items.add(item);
        }

        return items;
    }

}