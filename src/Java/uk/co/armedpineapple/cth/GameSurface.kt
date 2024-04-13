package uk.co.armedpineapple.cth

import android.content.Context
import android.view.SurfaceHolder
import org.libsdl.app.SDLSurface

/**
 * A surface that applies scaling to stretch the game window when required.
 */
class GameSurface(context: Context?) : SDLSurface(context) {
    private val configuration: GameConfiguration
        get() = (context.applicationContext as CTHApplication).configuration

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setScaling()
    }

    private fun setScaling() {
        if (!configuration.keepDisplayAspectRatio) {
            val targetRes = configuration.resolution
            if (width < height) {
                scaleX = 1.0f
                scaleY = (height * targetRes.first.toFloat()/width) / targetRes.second.toFloat()
            } else {
                scaleX = (width * targetRes.second.toFloat()/height) / targetRes.first.toFloat()
                scaleY = 1.0f
            }
        } else {
            scaleY = 1.0f
            scaleX = 1.0f
        }
    }
}
