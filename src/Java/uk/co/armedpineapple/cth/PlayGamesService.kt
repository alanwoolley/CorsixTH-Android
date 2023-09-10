package uk.co.armedpineapple.cth

import android.app.Activity
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.tasks.Task
import uk.co.armedpineapple.cth.stats.StatisticsService


class PlayGamesService(
    private val activity: Activity, private val statisticsService: StatisticsService
) : Loggable {

    private var achievementsTracker: AchievementsTracker

    init {
        val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask: Task<AuthenticationResult> ->
            val isAuthenticated =
                isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated
            if (isAuthenticated) {
                info { "Signed in with Google Play Games" }
            } else {
                error("Failed to sign in with Google Play Games", isAuthenticatedTask.exception)
            }
        }

        achievementsTracker = AchievementsTracker(activity, statisticsService)
    }
}