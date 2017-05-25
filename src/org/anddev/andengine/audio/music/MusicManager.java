package org.anddev.andengine.audio.music;

import org.anddev.andengine.audio.BaseAudioManager;
import org.anddev.andengine.audio.sound.SoundManager;
import org.anddev.andengine.engine.Engine;

/**
 * @author Nicolas Gramlich
 * @since 15:01:23 - 13.06.2010
 */
public class MusicManager extends BaseAudioManager<Music> {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public MusicManager() {

	}

	public SoundManager getSoundManager(Engine engine) throws IllegalStateException {
		if(engine.mSoundManager != null) {
			return engine.mSoundManager;
		} else {
			throw new IllegalStateException("To enable the SoundManager, check the EngineOptions!");
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
