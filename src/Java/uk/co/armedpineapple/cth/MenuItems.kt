package uk.co.armedpineapple.cth

enum class MenuItems(val textResource: Int?, val imageResource: Int?, private val debugOnly: Boolean) {
    GAME_SPEED(R.string.menuitem_change_game_speed, null, false),
    QUICK_LOAD(R.string.menuitem_quickload, null, false),
    QUICK_SAVE(R.string.menuitem_quicksave, null, false),
    LOAD(R.string.menuitem_load, null, false),
    SAVE(R.string.menuitem_save, null, false),
    RESTART(R.string.menuitem_restart, null, false),
    SETTINGS(R.string.menuitem_settings, null, false),
    JUKEBOX(R.string.menuitem_jukebox, null, false),
    CHEATS(R.string.menuitem_cheats, null, true),
    HELP(R.string.menuitem_help, null, false),
    ABOUT(R.string.menuitem_about, null, false),
    EXIT(R.string.menuitem_exit, null, false);

    companion object {
        fun getItems(inDebugMode: Boolean): List<MenuItems> {
            return MenuItems.values().filter { !it.debugOnly || it.debugOnly == inDebugMode }
        }
    }
}