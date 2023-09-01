package uk.co.armedpineapple.cth.stats

import uk.co.armedpineapple.cth.Loggable
import uk.co.armedpineapple.cth.persistence.stats.CampaignLevelCompleted
import uk.co.armedpineapple.cth.persistence.stats.StatsDatabase
import java.time.LocalDateTime

/**
 * A service for interacting with game statistics.
 *
 * @param statsDatabase The database containing the statistics
 */
class StatisticsService(statsDatabase: StatsDatabase) : Loggable {
    private val countableStatsDao = statsDatabase.countableStatsDao()
    private val campaignLevelsDao = statsDatabase.campaignLevelsDao()

    private val observers =
        mutableMapOf<Statistic, MutableSet<(stat: Statistic, newValue: Long) -> Unit>>()

    /**
     * Attaches an observer
     *
     * @param stat The stat to receive notifications about
     * @param observer The observer to be invoked
     */
    fun attach(stat: Statistic, observer: (stat: Statistic, newValue: Long) -> Unit) {
        synchronized(observers) {
            observers.computeIfAbsent(stat) { mutableSetOf() }.add(observer)
        }
    }

    /**
     * Detaches an observer
     *
     * @param stat The stat to stop receiving notifications about
     * @param observer The observer to stop invoking
     */
    fun detach(stat: Statistic, observer: (stat: Statistic, newValue: Long) -> Unit) {
        synchronized(observers) {
            observers[stat]?.remove(observer)
        }
    }

    /**
     * Increments a countable statistic
     *
     * @param stat The statistic
     * @param incrementBy The amount to increment by
     * @return The new value of the statistic
     */
    fun incrementStat(stat: Statistic, incrementBy : Long = 1): Long {
        val newValue = countableStatsDao.increment(stat.name, incrementBy)
        notify(stat, newValue)
        return newValue
    }

    /**
     * Mark a campaign level as completed.
     *
     * @param level The completed level
     * @param completedOn The date when the level was completed.
     */
    fun completeLevel(level: Int, completedOn: LocalDateTime) {
        campaignLevelsDao.upsert(CampaignLevelCompleted(level, completedOn))
    }

    private fun notify(stat: Statistic, newValue: Long) {
        var toNotify: Iterable<(stat: Statistic, newValue: Long) -> Unit>?

        synchronized(observers) {
            toNotify = observers[stat]
        }

        toNotify?.let {
            it.forEach { observer -> observer(stat, newValue) }
        }
    }
}