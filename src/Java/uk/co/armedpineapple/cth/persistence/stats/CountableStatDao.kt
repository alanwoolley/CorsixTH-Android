package uk.co.armedpineapple.cth.persistence.stats

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

/**
 * DAO for countable game statistics.
 */
@Dao
abstract class CountableStatDao {
    /**
     * Upsert a CountableStat
     *
     * @param stat The CountableStat
     */
    @Upsert
    abstract fun upsert(stat: CountableStat)

    /**
     * Gets all countable stats
     *
     * @return All countable stats
     */
    @Query("SELECT * from countablestats")
    abstract fun getAll(): List<CountableStat>

    /**
     * Gets a single CountableStat identified by its type
     *
     * @param name The stat type
     * @return The CountableStat corresponding to type
     */
    @Query("SELECT * from countablestats WHERE type = :type")
    abstract fun get(type: String): CountableStat?

    /**
     * Increments a stat and returns the new value
     *
     * @param type the type of stat
     * @param incrementBy the amount to increment by
     * @return the new value of the stat
     */
    @Transaction
    open fun increment(type: String, incrementBy: Long = 1): Long {
        val statItem = get(type) ?: CountableStat(type, 0)
        val newCount = statItem.count + incrementBy
        statItem.count = newCount
        upsert(statItem)
        return newCount
    }
}