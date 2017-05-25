package org.anddev.andengine.audio.sound;

import org.anddev.andengine.audio.BaseAudioManager;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.options.EngineOptions;

import android.media.AudioManager;
import android.media.SoundPool;

/**
 * @author Nicolas Gramlich
 * @since 13:22:59 - 11.03.2010
 */
public class SoundManager extends BaseAudioManager<Sound> {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int MAX_SIMULTANEOUS_STREAMS_DEFAULT = 5;

	// ===========================================================
	// Fields
	// ===========================================================

	private final SoundPool mSoundPool;

	// ===========================================================
	// Constructors
	// ===========================================================

	public SoundManager() {
		this(MAX_SIMULTANEOUS_STREAMS_DEFAULT);
	}

	public SoundManager(final int pMaxSimultaneousStreams) {
		this.mSoundPool = new SoundPool(pMaxSimultaneousStreams, AudioManager.STREAM_MUSIC, 0);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	SoundPool getSoundPool() {
		return this.mSoundPool;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	public void releaseAll() {
		super.releaseAll();

		this.mSoundPool.release();
	}

	public EngineOptions getEngineOptions(Engine engine) {
		return engine.mEngineOptions;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
