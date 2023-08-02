package uk.co.armedpineapple.cth.files

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import uk.co.armedpineapple.cth.R

/**
 * An activity for loading or saving games.
 */
class SaveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.save_activity)
        val isLoad = intent.getBooleanExtra(EXTRA_IS_LOAD, false);
        if (savedInstanceState == null) {
            val newFragment = SaveLoadFragment.newInstance(isLoad)

            supportFragmentManager.beginTransaction().replace(R.id.save, newFragment).commit()

            newFragment.setFragmentResultListener(SaveLoadFragment.REQUEST_SELECT) { _, bundle ->
                val fileName = bundle.getString(SaveLoadFragment.BUNDLE_FILENAME)
                setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_SAVE_GAME_NAME, fileName))
                finish()
            }
        }

        title = when (isLoad) {
            false -> getString(R.string.save_game)
            true -> getString(R.string.load_game)
        }
    }

    companion object {
        const val EXTRA_SAVE_GAME_NAME: String = "extra.savegamename"
        const val EXTRA_IS_LOAD: String = "extra.isload"
    }
}