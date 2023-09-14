package uk.co.armedpineapple.cth.files

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import uk.co.armedpineapple.cth.R


/**
 * An activity for loading or saving games.
 */
class SaveActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.save_activity)
        setSupportActionBar(findViewById(R.id.toolbar))
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

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
        (findViewById<TextView>(R.id.toolbar_title)).text = title
    }

    companion object {
        const val EXTRA_SAVE_GAME_NAME: String = "extra.savegamename"
        const val EXTRA_IS_LOAD: String = "extra.isload"
    }
}