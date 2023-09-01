package uk.co.armedpineapple.cth.persistence.stats

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import uk.co.armedpineapple.cth.persistence.Converters

/**
 * Game statistics database.
 */
@Database(entities = [CampaignLevelCompleted::class, CountableStat::class], version = 1)
@TypeConverters(Converters::class)
abstract class StatsDatabase : RoomDatabase(){
    /**
     * Gets a DAO for countable stats
     */
    abstract fun countableStatsDao() : CountableStatDao

    /**
     * Gets a DAO for completed campaign levels
     */
    abstract fun campaignLevelsDao() : CampaignLevelCompletedDao
}