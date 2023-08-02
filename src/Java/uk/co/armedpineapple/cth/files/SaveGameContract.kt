package uk.co.armedpineapple.cth.files

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

/**
 * A contract for requesting either loading or saving a save game.
 */
class SaveGameContract : ActivityResultContract<Boolean, String?>() {
    override fun createIntent(context: Context, input: Boolean): Intent {
        return Intent(context, SaveActivity::class.java).putExtra(SaveActivity.EXTRA_IS_LOAD, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }

        return intent?.getStringExtra(SaveActivity.EXTRA_SAVE_GAME_NAME)
    }
}
