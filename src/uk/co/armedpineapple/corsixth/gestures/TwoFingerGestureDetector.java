package uk.co.armedpineapple.corsixth.gestures;

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This is a stripped down version of ScaleGestureDetector.java in order to 
 * better serve CTH's need 
 *  
 * Modifications Alan Woolley 2012
 */

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class TwoFingerGestureDetector {

	public interface OnTwoFingerGestureListener {

		public boolean onTwoFingerEvent(TwoFingerGestureDetector detector);

		public boolean onTwoFingerBegin(TwoFingerGestureDetector detector);

		public void onTwoFingerEnd(TwoFingerGestureDetector detector);
	}

	private static final float								PRESSURE_THRESHOLD	= 0.67f;

	private final Context											mContext;
	private final OnTwoFingerGestureListener	mListener;
	private boolean														mGestureInProgress;

	private MotionEvent												mPrevEvent, mCurrEvent;

	private float															mFocusX, mFocusY, mCurrPressure,
			mPrevPressure;

	private final float												mEdgeSlop;

	private float															mRightSlopEdge, mBottomSlopEdge;
	private boolean														mSloppyGesture;

	public TwoFingerGestureDetector(Context context,
			OnTwoFingerGestureListener listener) {
		ViewConfiguration config = ViewConfiguration.get(context);
		mContext = context;
		mListener = listener;
		mEdgeSlop = config.getScaledEdgeSlop();
	}

	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		boolean handled = true;

		if (!mGestureInProgress) {
			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_DOWN: {
					// We have a new multi-finger gesture

					// as orientation can change, query the metrics in touch down
					DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
					mRightSlopEdge = metrics.widthPixels - mEdgeSlop;
					mBottomSlopEdge = metrics.heightPixels - mEdgeSlop;

					// Be paranoid in case we missed an event
					reset();

					mPrevEvent = MotionEvent.obtain(event);

					setContext(event);

					// Check if we have a sloppy gesture. If so, delay
					// the beginning of the gesture until we're sure that's
					// what the user wanted. Sloppy gestures can happen if the
					// edge of the user's hand is touching the screen, for example.
					final float edgeSlop = mEdgeSlop;
					final float rightSlop = mRightSlopEdge;
					final float bottomSlop = mBottomSlopEdge;
					final float x0 = event.getRawX();
					final float y0 = event.getRawY();
					final float x1 = getRawX(event, 1);
					final float y1 = getRawY(event, 1);

					boolean p0sloppy = x0 < edgeSlop || y0 < edgeSlop || x0 > rightSlop
							|| y0 > bottomSlop;
					boolean p1sloppy = x1 < edgeSlop || y1 < edgeSlop || x1 > rightSlop
							|| y1 > bottomSlop;

					if (p0sloppy && p1sloppy) {
						mFocusX = -1;
						mFocusY = -1;
						mSloppyGesture = true;
					} else if (p0sloppy) {
						mFocusX = event.getX(1);
						mFocusY = event.getY(1);
						mSloppyGesture = true;
					} else if (p1sloppy) {
						mFocusX = event.getX(0);
						mFocusY = event.getY(0);
						mSloppyGesture = true;
					} else {
						mGestureInProgress = mListener.onTwoFingerBegin(this);
					}
				}
					break;

				case MotionEvent.ACTION_MOVE:
					if (mSloppyGesture) {
						// Initiate sloppy gestures if we've moved outside of the slop area.
						final float edgeSlop = mEdgeSlop;
						final float rightSlop = mRightSlopEdge;
						final float bottomSlop = mBottomSlopEdge;
						final float x0 = event.getRawX();
						final float y0 = event.getRawY();
						final float x1 = getRawX(event, 1);
						final float y1 = getRawY(event, 1);

						boolean p0sloppy = x0 < edgeSlop || y0 < edgeSlop || x0 > rightSlop
								|| y0 > bottomSlop;
						boolean p1sloppy = x1 < edgeSlop || y1 < edgeSlop || x1 > rightSlop
								|| y1 > bottomSlop;

						if (p0sloppy && p1sloppy) {
							mFocusX = -1;
							mFocusY = -1;
						} else if (p0sloppy) {
							mFocusX = event.getX(1);
							mFocusY = event.getY(1);
						} else if (p1sloppy) {
							mFocusX = event.getX(0);
							mFocusY = event.getY(0);
						} else {
							mSloppyGesture = false;
							mGestureInProgress = mListener.onTwoFingerBegin(this);
						}
					}
					break;

				case MotionEvent.ACTION_POINTER_UP:
					if (mSloppyGesture) {
						// Set focus point to the remaining finger
						int id = (((action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT) == 0) ? 1
								: 0;
						mFocusX = event.getX(id);
						mFocusY = event.getY(id);
					}
					break;
			}
		} else {
			// Transform gesture in progress - attempt to handle it
			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_POINTER_UP:
					// Gesture ended
					setContext(event);

					// Set focus point to the remaining finger
					int id = (((action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT) == 0) ? 1
							: 0;
					mFocusX = event.getX(id);
					mFocusY = event.getY(id);

					if (!mSloppyGesture) {
						mListener.onTwoFingerEnd(this);
					}

					reset();
					break;

				case MotionEvent.ACTION_CANCEL:
					if (!mSloppyGesture) {
						mListener.onTwoFingerEnd(this);
					}

					reset();
					break;

				case MotionEvent.ACTION_MOVE:
					setContext(event);

					// Only accept the event if our relative pressure is within
					// a certain limit - this can help filter shaky data as a
					// finger is lifted.
					if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
						final boolean updatePrevious = mListener.onTwoFingerEvent(this);

						if (updatePrevious) {
							mPrevEvent.recycle();
							mPrevEvent = MotionEvent.obtain(event);
						}
					}
					break;
			}
		}
		return handled;
	}

	private static float getRawX(MotionEvent event, int pointerIndex) {
		float offset = event.getX() - event.getRawX();
		return event.getX(pointerIndex) + offset;
	}

	private static float getRawY(MotionEvent event, int pointerIndex) {
		float offset = event.getY() - event.getRawY();
		return event.getY(pointerIndex) + offset;
	}

	private void setContext(MotionEvent curr) {
		if (mCurrEvent != null) {
			mCurrEvent.recycle();
		}
		mCurrEvent = MotionEvent.obtain(curr);

		final MotionEvent prev = mPrevEvent;

		final float cx0 = curr.getX(0);
		final float cy0 = curr.getY(0);
		final float cx1 = curr.getX(1);
		final float cy1 = curr.getY(1);

		final float cvx = cx1 - cx0;
		final float cvy = cy1 - cy0;

		mFocusX = cx0 + cvx * 0.5f;
		mFocusY = cy0 + cvy * 0.5f;
		mCurrPressure = curr.getPressure(0) + curr.getPressure(1);
		mPrevPressure = prev.getPressure(0) + prev.getPressure(1);
	}

	private void reset() {
		if (mPrevEvent != null) {
			mPrevEvent.recycle();
			mPrevEvent = null;
		}
		if (mCurrEvent != null) {
			mCurrEvent.recycle();
			mCurrEvent = null;
		}
		mSloppyGesture = false;
		mGestureInProgress = false;
	}

	public boolean isInProgress() {
		return mGestureInProgress;
	}

	public float getFocusX() {
		return mFocusX;
	}

	public float getFocusY() {
		return mFocusY;
	}

}