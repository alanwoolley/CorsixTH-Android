package uk.co.armedpineapple.cth

import android.app.Activity
import com.google.android.gms.games.PlayGames
import uk.co.armedpineapple.cth.stats.Statistic
import uk.co.armedpineapple.cth.stats.StatisticsService

/**
 * Tracks achievement progress.
 *
 * @property activity The activity
 *
 * @param statisticsService The statistics service
 */
class AchievementsTracker(
    private val activity: Activity, statisticsService: StatisticsService
) : Loggable {

    private var client = PlayGames.getAchievementsClient(activity)

    private val levelAchievements = arrayOf(
        R.string.achievement_wasteland_wizard,
        R.string.achievement_nightmare_nixer,
        R.string.achievement_mega_mogul,
        R.string.achievement_cosmic_captain,
        R.string.achievement_zen_master,
        R.string.achievement_plague_pioneer,
        R.string.achievement_epidemic_emperor,
        R.string.achievement_dirt_dynamo,
        R.string.achievement_outbreak_obliterator,
        R.string.achievement_clucking_conqueror,
        R.string.achievement_alien_alchemist,
        R.string.achievement_cake_commander
    )

    init {
        statisticsService.attach(Statistic.CURES, ::onCuresChanged)
        statisticsService.attach(Statistic.MONEY_EARNED, ::onMoneyEarned)
        statisticsService.attach(Statistic.KILLS, ::onKillsChanged)
        statisticsService.attach(Statistic.LOAN_TAKEN, ::onLoanTaken)
        statisticsService.attach(Statistic.MONEY_LOST, ::onMoneyLost)
        statisticsService.attach(Statistic.LEVEL_COMPLETED, ::onLevelCompleted)
    }

    private fun onLevelCompleted(statistic: Statistic, level: Long) {
        if (level >= levelAchievements.size) {
            error { "Completed a level outside of the expected campaign levels. ($level)" }
            return
        }

        client.unlock(activity.getString(levelAchievements[level.toInt() - 1]))
    }

    private fun onMoneyLost(statistic: Statistic, totalLost: Long) {
        val thousands = (totalLost / 1000).toInt()
        client.setSteps(activity.getString(R.string.achievement_budget_breaker), thousands)
        client.setSteps(activity.getString(R.string.achievement_fiscal_feats), thousands)
        client.setSteps(activity.getString(R.string.achievement_opulent_outlays), thousands)
        client.setSteps(activity.getString(R.string.achievement_lavish_investments), thousands)
    }

    private fun onLoanTaken(statistic: Statistic, totalBorrowed: Long) {
        val thousands = (totalBorrowed / 1000).toInt()
        client.setSteps(activity.getString(R.string.achievement_bucks_for_bedsheets), thousands)
        client.setSteps(
            activity.getString(R.string.achievement_moolah_from_makebelieve), thousands
        )
    }

    private fun onKillsChanged(statistic: Statistic, totalKills: Long) {
        if (totalKills > 0) client.unlock(activity.getString(R.string.achievement_first_do_no_harm))

        client.setSteps(
            activity.getString(R.string.achievement_oops_my_scalpel_slipped), totalKills.toInt()
        )
        client.setSteps(
            activity.getString(R.string.achievement_lifes_little_accidents), totalKills.toInt()
        )
        client.setSteps(
            activity.getString(R.string.achievement_statistical_unlikelihood), totalKills.toInt()
        )
        client.setSteps(
            activity.getString(R.string.achievement_master_of_the_macabre), totalKills.toInt()
        )
    }

    private fun onMoneyEarned(statistic: Statistic, totalEarned: Long) {
        val thousands = (totalEarned / 1000).toInt()
        client.setSteps(activity.getString(R.string.achievement_coins_in_the_coffers), thousands)
        client.setSteps(activity.getString(R.string.achievement_milliondollar_diagnosis), thousands)
        client.setSteps(activity.getString(R.string.achievement_cashflow_carnival), thousands)
        client.setSteps(activity.getString(R.string.achievement_decadollar_doctoring), thousands)
    }

    private fun onCuresChanged(statistic: Statistic, totalCures: Long) {
        if (totalCures > 0) client.unlock(activity.getString(R.string.achievement_firstaid_hero))

        client.setSteps(
            activity.getString(R.string.achievement_centennial_savior), totalCures.toInt()
        )
        client.setSteps(
            activity.getString(R.string.achievement_kilopatient_miracle_worker), totalCures.toInt()
        )

        val thousands = (totalCures / 1000).toInt()

        client.setSteps(activity.getString(R.string.achievement_mega_remedies_maestro), thousands)
        client.setSteps(
            activity.getString(R.string.achievement_halfmillion_healing_hero), thousands
        )
        client.setSteps(activity.getString(R.string.achievement_millionpatient_virtuoso), thousands)
    }
}