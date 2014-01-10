package uk.co.armedpineapple.cth.spen;

import uk.co.armedpineapple.cth.SDLActivity;
import android.view.MotionEvent;
import android.view.View;

import com.samsung.spensdk.applistener.SPenTouchListener;

/**
 * Touch listener implementing glue code for supporting input methods by
 * Samsung's SPen hardware used with the Galaxy Note family.
 * 
 * @author inagy
 */
public class SamsungSPenTouchListener implements SPenTouchListener {

	@Override
	public boolean onTouchPenEraser(View arg0, MotionEvent arg1) {
		// Not interested
		return false;
	}

	@Override
	public boolean onTouchPen(View arg0, MotionEvent arg1) {
		SDLActivity.mSurface.onTouch(arg0, arg1);
		return true;
	}

	@Override
	public boolean onTouchFinger(View arg0, MotionEvent arg1) {
		SDLActivity.mSurface.onTouch(arg0, arg1);
		return true;
	}

	@Override
	public void onTouchButtonUp(View arg0, MotionEvent arg1) {
		// Not interessted
	}

	@Override
	public void onTouchButtonDown(View arg0, MotionEvent arg1) {
		// Not interessted
	}
}