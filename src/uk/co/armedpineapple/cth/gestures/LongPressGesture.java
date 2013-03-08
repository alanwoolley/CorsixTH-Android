/*
 *   Copyright (C) 2012 Alan Woolley
 *   
 *   See LICENSE.TXT for full license
 */
package uk.co.armedpineapple.cth.gestures;

import uk.co.armedpineapple.cth.CTHApplication;
import uk.co.armedpineapple.cth.SDLActivity;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public class LongPressGesture implements OnGestureListener {

	private CTHApplication	appCtx;

	public LongPressGesture(Context ctx) {
		appCtx = (CTHApplication) ctx.getApplicationContext();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		Log.d(getClass().getSimpleName(), "Detected long press");

		final int touchDevId = event.getDeviceId();
		final int pointerCount = event.getPointerCount();
		int actionPointerIndex = event.getActionIndex();
		int pointerFingerId = event.getPointerId(actionPointerIndex);

		float[] coords = SDLActivity.mSurface.translateCoords(
				event.getX(actionPointerIndex), event.getY(actionPointerIndex));

		SDLActivity.onNativeTouch(touchDevId, pointerFingerId, 0, coords[0],
				coords[1], event.getPressure(actionPointerIndex), pointerCount, 1,
				appCtx.configuration.getControlsMode());

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

}
