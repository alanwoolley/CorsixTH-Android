package uk.co.armedpineapple.cth.gestures;

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
import android.util.Log;
import android.view.MotionEvent;

public class TwoFingerGestureDetector {
	
	private static final String	LOG_TAG	= "TwoFingerDetector";

	public interface OnTwoFingerGestureListener {

		public boolean onTwoFingerEvent(TwoFingerGestureDetector detector);

		public boolean onTwoFingerBegin(TwoFingerGestureDetector detector);

		public void onTwoFingerEnd(TwoFingerGestureDetector detector);
	}

	private static final float								PRESSURE_THRESHOLD	= 0.67f;

	private final OnTwoFingerGestureListener	mListener;

	private MotionEvent												mPrevEvent, mCurrEvent;

	private float															mFocusX, mFocusY;

	private float															mCurrPressure, mPrevPressure;

	private boolean														mInvalidGesture,
			mGestureInProgress;

	// Pointer IDs currently responsible for the two fingers controlling the
	// gesture

	private int																mActiveId0, mActiveId1;
	private boolean														mActive0MostRecent;

	public TwoFingerGestureDetector(Context context,
			OnTwoFingerGestureListener listener) {

		mListener = listener;

	}

	public boolean onTouchEvent(MotionEvent event) {

		final int action = event.getActionMasked();

		if (action == MotionEvent.ACTION_DOWN) {
			reset(); // Start fresh
		}

		boolean handled = true;
		if (mInvalidGesture) {
			handled = false;
		} else if (!mGestureInProgress) {
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					mActiveId0 = event.getPointerId(0);
					mActive0MostRecent = true;
				}
					break;

				case MotionEvent.ACTION_UP:
					reset();
					break;

				case MotionEvent.ACTION_POINTER_DOWN: {
					// We have a new multi-finger gesture
					if (mPrevEvent != null)
						mPrevEvent.recycle();
					mPrevEvent = MotionEvent.obtain(event);

					int index1 = event.getActionIndex();
					int index0 = event.findPointerIndex(mActiveId0);
					mActiveId1 = event.getPointerId(index1);
					if (index0 < 0 || index0 == index1) {
						// Probably someone sending us a broken event stream.
						index0 = findNewActiveIndex(event, mActiveId1, -1);
						mActiveId0 = event.getPointerId(index0);
					}
					mActive0MostRecent = false;

					setContext(event);

					mGestureInProgress = mListener.onTwoFingerBegin(this);
					break;
				}
			}
		} else {
			// Transform gesture in progress - attempt to handle it
			switch (action) {
				case MotionEvent.ACTION_POINTER_DOWN: {
					// End the old gesture and begin a new one with the most recent two
					// fingers.
					mListener.onTwoFingerEnd(this);
					final int oldActive0 = mActiveId0;
					final int oldActive1 = mActiveId1;
					reset();

					mPrevEvent = MotionEvent.obtain(event);
					mActiveId0 = mActive0MostRecent ? oldActive0 : oldActive1;
					mActiveId1 = event.getPointerId(event.getActionIndex());
					mActive0MostRecent = false;

					int index0 = event.findPointerIndex(mActiveId0);
					if (index0 < 0 || mActiveId0 == mActiveId1) {
						index0 = findNewActiveIndex(event, mActiveId1, -1);
						mActiveId0 = event.getPointerId(index0);
					}

					setContext(event);

					mGestureInProgress = mListener.onTwoFingerBegin(this);
				}
					break;

				case MotionEvent.ACTION_POINTER_UP: {
					final int pointerCount = event.getPointerCount();
					final int actionIndex = event.getActionIndex();
					final int actionId = event.getPointerId(actionIndex);

					boolean gestureEnded = false;
					if (pointerCount > 2) {
						if (actionId == mActiveId0) {
							final int newIndex = findNewActiveIndex(event, mActiveId1,
									actionIndex);
							if (newIndex >= 0) {
								mListener.onTwoFingerEnd(this);
								mActiveId0 = event.getPointerId(newIndex);
								mActive0MostRecent = true;
								mPrevEvent = MotionEvent.obtain(event);
								setContext(event);
								mGestureInProgress = mListener.onTwoFingerBegin(this);
							} else {
								gestureEnded = true;
							}
						} else if (actionId == mActiveId1) {
							final int newIndex = findNewActiveIndex(event, mActiveId0,
									actionIndex);
							if (newIndex >= 0) {
								mListener.onTwoFingerEnd(this);
								mActiveId1 = event.getPointerId(newIndex);
								mActive0MostRecent = false;
								mPrevEvent = MotionEvent.obtain(event);
								setContext(event);
								mGestureInProgress = mListener.onTwoFingerBegin(this);
							} else {
								gestureEnded = true;
							}
						}
						mPrevEvent.recycle();
						mPrevEvent = MotionEvent.obtain(event);
						setContext(event);
					} else {
						gestureEnded = true;
					}

					if (gestureEnded) {
						// Gesture ended
						setContext(event);

						// Set focus point to the remaining finger
						final int activeId = actionId == mActiveId0 ? mActiveId1
								: mActiveId0;
						final int index = event.findPointerIndex(activeId);
						mFocusX = event.getX(index);
						mFocusY = event.getY(index);

						mListener.onTwoFingerEnd(this);
						reset();
						mActiveId0 = activeId;
						mActive0MostRecent = true;
					}
				}
					break;

				case MotionEvent.ACTION_CANCEL:
					mListener.onTwoFingerEnd(this);
					reset();
					break;

				case MotionEvent.ACTION_UP:
					reset();
					break;

				case MotionEvent.ACTION_MOVE: {
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
				}
					break;
			}
		}

		return handled;
	}

	private int findNewActiveIndex(MotionEvent ev, int otherActiveId,
			int removedPointerIndex) {
		final int pointerCount = ev.getPointerCount();

		// It's ok if this isn't found and returns -1, it simply won't match.
		final int otherActiveIndex = ev.findPointerIndex(otherActiveId);

		// Pick a new id and update tracking state.
		for (int i = 0; i < pointerCount; i++) {
			if (i != removedPointerIndex && i != otherActiveIndex) {
				return i;
			}
		}
		return -1;
	}

	private void setContext(MotionEvent curr) {
		if (mCurrEvent != null) {
			mCurrEvent.recycle();
		}
		mCurrEvent = MotionEvent.obtain(curr);

		final MotionEvent prev = mPrevEvent;

		final int prevIndex0 = prev.findPointerIndex(mActiveId0);
		final int prevIndex1 = prev.findPointerIndex(mActiveId1);
		final int currIndex0 = curr.findPointerIndex(mActiveId0);
		final int currIndex1 = curr.findPointerIndex(mActiveId1);

		if (prevIndex0 < 0 || prevIndex1 < 0 || currIndex0 < 0 || currIndex1 < 0) {
			mInvalidGesture = true;
			Log.e(LOG_TAG, "Invalid MotionEvent stream detected.",
					new Throwable());
			if (mGestureInProgress) {
				mListener.onTwoFingerEnd(this);
			}
			return;
		}

		final float cx0 = curr.getX(currIndex0);
		final float cy0 = curr.getY(currIndex0);
		final float cx1 = curr.getX(currIndex1);
		final float cy1 = curr.getY(currIndex1);

		final float cvx = cx1 - cx0;
		final float cvy = cy1 - cy0;

		mFocusX = cx0 + cvx * 0.5f;
		mFocusY = cy0 + cvy * 0.5f;

		mCurrPressure = curr.getPressure(currIndex0) + curr.getPressure(currIndex1);
		mPrevPressure = prev.getPressure(prevIndex0) + prev.getPressure(prevIndex1);
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
		mGestureInProgress = false;
		mActiveId0 = -1;
		mActiveId1 = -1;
		mInvalidGesture = false;
	}

	/**
	 * Returns {@code true} if a two-finger gesture is in progress.
	 * 
	 * @return {@code true} if a two-finger gesture is in progress, {@code false}
	 *         otherwise.
	 */
	public boolean isInProgress() {
		return mGestureInProgress;
	}

	/**
	 * Get the X coordinate of the current gesture's focal point. If a gesture is
	 * in progress, the focal point is directly between the two pointers forming
	 * the gesture. If a gesture is ending, the focal point is the location of the
	 * remaining pointer on the screen. If {@link #isInProgress()} would return
	 * false, the result of this function is undefined.
	 * 
	 * @return X coordinate of the focal point in pixels.
	 */
	public float getFocusX() {
		return mFocusX;
	}

	/**
	 * Get the Y coordinate of the current gesture's focal point. If a gesture is
	 * in progress, the focal point is directly between the two pointers forming
	 * the gesture. If a gesture is ending, the focal point is the location of the
	 * remaining pointer on the screen. If {@link #isInProgress()} would return
	 * false, the result of this function is undefined.
	 * 
	 * @return Y coordinate of the focal point in pixels.
	 */
	public float getFocusY() {
		return mFocusY;
	}

	/**
	 * Return the event time of the current event being processed.
	 * 
	 * @return Current event time in milliseconds.
	 */
	public long getEventTime() {
		return mCurrEvent.getEventTime();
	}

}