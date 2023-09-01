package uk.co.armedpineapple.cth.persistence.saves

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Data relating to a saved game
 *
 * @property saveName The name of the saved game. This should match the filename of the saved game file.
 * @property screenshotPath A path to the screenshot of the saved game.
 * @property rep The amount of reputation.
 * @property money The bank balance.
 * @property levelName The name of the level.
 */
@Entity(tableName = "saves")
data class SaveData(
    @PrimaryKey @ColumnInfo(name = "save_name") val saveName: String,

    @ColumnInfo(name = "screenshot") var screenshotPath: String? = null,

    @ColumnInfo(name = "rep") var rep: Int? = null,

    @ColumnInfo(name = "money") var money: Long? = null,

    @ColumnInfo(name = "level_name") var levelName: String? = null
)
{
    /**
     * Gets the date of the save.
     */
    @Ignore var saveDate: LocalDateTime? = null
}

/**
 * Gets an identifier for SaveData by name
 *
 * @property saveName The save game name.
 */
data class SaveName(
    @ColumnInfo(name = "save_name") val saveName: String
)
