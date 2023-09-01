package uk.co.armedpineapple.cth.persistence.saves

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Game database. This stores non-essential game metadata.
 */
@Database(entities = [SaveData::class], version = 1)
abstract class GameDatabase : RoomDatabase() {
    /**
     * Gets a DAO for save game information
     *
     * @return A SaveDao
     */
    abstract fun saveDao() : SaveDao
}