package org.anddev.andengine.sensor.orientation;

import java.util.Arrays;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.sensor.BaseSensorData;
import org.anddev.andengine.util.Debug;

import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Nicolas Gramlich
 * @since 11:30:33 - 25.05.2010
 */
public class OrientationData extends BaseSensorData {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public OrientationData() {
		super(3);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public float getRoll() {
		return super.mValues[SensorManager.DATA_Z];
	}

	public float getPitch() {
		return super.mValues[SensorManager.DATA_Y];
	}

	public float getYaw() {
		return super.mValues[SensorManager.DATA_X];
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public String toString() {
		return "Orientation: " + Arrays.toString(this.mValues);
	}

	public boolean onTouch(Engine engine, final View pView, final MotionEvent pSurfaceMotionEvent) {
		if(engine.mRunning) {
			final boolean handled = engine.mTouchController.onHandleMotionEvent(pSurfaceMotionEvent, engine);
			try {
				/*
				 * As a human cannot interact 1000x per second, we pause the
				 * UI-Thread for a little.
				 */
				Thread.sleep(20);
			} catch (final InterruptedException e) {
				Debug.e(e);
			}
			return handled;
		} else {
			return false;
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
