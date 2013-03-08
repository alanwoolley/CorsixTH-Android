/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.gestures;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import uk.co.armedpineapple.cth.CTHApplication;
import uk.co.armedpineapple.cth.SDLActivity;

public class TwoFingerMoveGesture implements
		TwoFingerGestureDetector.OnTwoFingerGestureListener {

	private final float			delta	= 10;

	private float						prevX, prevY, originX, originY;

	private boolean					first	= false;

	private CTHApplication	appCtx;

	public TwoFingerMoveGesture(Context ctx) {
		appCtx = (CTHApplication) ctx.getApplicationContext();
	}

	@Override
	public boolean onTwoFingerEvent(TwoFingerGestureDetector detector) {

		float[] coords = SDLActivity.mSurface.translateCoords(detector.getFocusX(),
				detector.getFocusY());
		prevX = coords[0];
		prevY = coords[1];
		if (first) {
			// Check to see if we've moved a suitable distance. By doing this,
			// we can make the start of the gesture much smoother.
			if ((Math.abs(originX - prevX) > delta || Math.abs(originY - prevY) > delta)) {

				SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_DOWN, prevX, prevY,
						0, 2, 2, appCtx.configuration.getControlsMode());
				first = false;
			}

		} else {

			SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_MOVE, prevX, prevY, 0,
					2, 2, appCtx.configuration.getControlsMode());
		}

		return true;
	}

	@Override
	public boolean onTwoFingerBegin(TwoFingerGestureDetector detector) {
		Log.d(getClass().getSimpleName(), "Move gesture - BEGIN");
		first = true;
		float[] coords = SDLActivity.mSurface.translateCoords(detector.getFocusX(),
				detector.getFocusY());
		// Record the coordinates that triggered the gesture
		originX = coords[0];
		originY = coords[1];

		return true;
	}

	@Override
	public void onTwoFingerEnd(TwoFingerGestureDetector detector) {
		Log.d(getClass().getSimpleName(), "Move gesture - END");

		if (!first) {

			SDLActivity.onNativeTouch(0, 0, MotionEvent.ACTION_UP, prevX, prevY, 0,
					2, 2, appCtx.configuration.getControlsMode());
		}
	}

}
