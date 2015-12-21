package uk.co.armedpineapple.cth.spen;

import uk.co.armedpineapple.cth.SDLActivity;
import android.view.MotionEvent;
import android.view.View;

import com.samsung.spensdk.applistener.SPenHoverListener;

/**
 * Hover listener implementing glue code for supporting input methods by
 * Samsung's SPen hardware used with the Galaxy Note family. Moves the in game
 * mouse cursor when the Pen is hovering the screen. Emulates right mouse clicks
 * in the game when the pen is above the screen and the side button is pressed.
 * 
 * @author inagy
 */
public class SamsungSPenHoverListener implements SPenHoverListener {

	private static final boolean	PEN_BUTTON_NOT_PRESSED	= false;
	private static final boolean	PEN_BUTTON_PRESSED			= true;

	private boolean								currentPenButtonState		= PEN_BUTTON_NOT_PRESSED;

	@Override
	public void onHoverButtonUp(View arg0, MotionEvent arg1) {
		if (PEN_BUTTON_PRESSED == currentPenButtonState) {
			//SDLActivity.onSpenButton();
		}

		currentPenButtonState = PEN_BUTTON_NOT_PRESSED;
	}

	@Override
	public void onHoverButtonDown(View arg0, MotionEvent arg1) {
		currentPenButtonState = PEN_BUTTON_PRESSED;
	}

	@Override
	public boolean onHover(View arg0, MotionEvent event) {
		float[] coords = SDLActivity.mSurface.translateCoords(event.getX(0),
				event.getY(0));
		SDLActivity.onNativeMouse(0, MotionEvent.ACTION_HOVER_MOVE, coords[0], coords[1]);
		return true;
	}
}