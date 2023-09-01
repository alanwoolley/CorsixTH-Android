package uk.co.armedpineapple.cth.persistence.stats

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

/**
 * DAO for completed campaign levels
 */
@Dao
interface CampaignLevelCompletedDao {
    /**
     * Upsert a CampaignLevelCompleted
     *
     * @param stat The CampaignLevelCompleted
     */
    @Upsert
    fun upsert(level: CampaignLevelCompleted)

    /**
     * Gets all completed campaign levels
     *
     * @return All completed campaign levels
     */
    @Query("SELECT * from campaignlevels")
    fun getAll(): List<CampaignLevelCompleted>

    /**
     * Gets a single CountableStat identified by its type
     *
     * @param name The stat type
     * @return The CountableStat corresponding to type
     */
    @Query("SELECT * from campaignlevels WHERE level = :level")
    fun get(level: Int): CampaignLevelCompleted?

}