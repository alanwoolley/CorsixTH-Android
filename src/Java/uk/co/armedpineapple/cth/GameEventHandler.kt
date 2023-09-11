package uk.co.armedpineapple.cth

import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.co.armedpineapple.cth.stats.Statistic
import uk.co.armedpineapple.cth.stats.StatisticsService
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Handles in-game events.
 *
 * An event is anything interesting that happens within a game session that we might want to keep
 * track of. For example, when a patient is cured or killed, or a level is won.
 *
 * @property statsService The StatisticsService
 */
class GameEventHandler(private val statsService: StatisticsService) : Loggable {

    /**
     * Handle a patient being cured.
     */
    @Keep
    fun onCure() {
        info { "Patient Cured" }
        doAsync { statsService.incrementStat(Statistic.CURES) }
    }

    /**
     * Handle a patient being killed.
     */
    @Keep
    fun onKill() {
        info { "Patient Killed" }
        doAsync { statsService.incrementStat(Statistic.KILLS) }
    }

    /**
     * Handle a campaign level being won.
     *
     * @param level The level
     */
    @Keep
    fun onCampaignLevelComplete(level: Int) {
        if (level < 0 || level > 12) throw IllegalArgumentException("Invalid level: $level");

        doAsync { statsService.completeLevel(level, LocalDateTime.now()) }
    }

    /**
     * Handle a change in bank balance.
     *
     * This will be called in addition to onLoanTaken, if a loan is taken.
     *
     * @param delta The change in bank balance.
     */
    @Keep
    fun onBankBalanceChanged(delta: Long) {
        info { "Bank balance changed by: $delta" }

        if (delta.absoluteValue < 10000000) {
            if (delta.sign == 1) {
                doAsync { statsService.incrementStat(Statistic.MONEY_EARNED, delta) }
            } else {
                doAsync { statsService.incrementStat(Statistic.MONEY_LOST, abs(delta)) }
            }
        } else {
            error { "Bank balance change ignored. Out of range. ($delta)" }
        }
    }

    /**
     * Handle a loan being taken.
     *
     * onBankBalanceChanged will also be called.
     *
     * @param amount The loan amount.
     */
    @Keep
    fun onLoanTaken(amount: Long) {
        info { "Loan taken: $amount" }

        if (amount < 10000000) {
            doAsync { statsService.incrementStat(Statistic.LOAN_TAKEN, amount) }
        } else {
            error { "Loan taken ignored. Out of range. ($amount)" }
        }
    }

    private fun doAsync(action: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            action()
        }
    }
}