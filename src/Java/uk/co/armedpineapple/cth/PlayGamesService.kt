package uk.co.armedpineapple.cth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.games.AuthenticationResult
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.Player
import com.google.android.gms.games.provider.PlayGamesInitProvider
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import uk.co.armedpineapple.cth.stats.StatisticsService


class PlayGamesService(
    private val activity: Activity, statisticsService: StatisticsService
) : Loggable {

    private var achievementsClient = PlayGames.getAchievementsClient(activity)
    private var achievementsTracker: AchievementsTracker =
        AchievementsTracker(activity, statisticsService, achievementsClient)
    private val gamesSignInClient = PlayGames.getGamesSignInClient(activity)
    private val playersClient = PlayGames.getPlayersClient(activity)

    var isAuthenticated: Boolean = false

    init {
        // Deliberately signing in shouldn't be necessary according to the SDK docs, but
        // the authentication always fails otherwise.
        gamesSignInClient.signIn()
        playersClient.currentPlayer.addOnCompleteListener(::onCurrentPlayerChanged)
        gamesSignInClient.isAuthenticated.addOnCompleteListener(::onAuthenticationChanged)
    }

    fun signIn() {
        gamesSignInClient.signIn().addOnCompleteListener(::onAuthenticationChanged)
    }

    fun showAchievements() {
        achievementsClient.achievementsIntent.addOnSuccessListener {
            activity.startActivityForResult(it, 1235212);
        }
    }

    private fun onAuthenticationChanged(result: Task<AuthenticationResult>) {
        this.isAuthenticated = result.isSuccessful && result.result.isAuthenticated
        if (isAuthenticated) {
            info { "Signed in with Google Play Games" }
        } else {
            error("Failed to sign in with Google Play Games", result.exception)
        }
    }

    private fun onCurrentPlayerChanged(player: Task<Player>) {
        if (player.isSuccessful) {
            // TODO - report back
        }
    }
}