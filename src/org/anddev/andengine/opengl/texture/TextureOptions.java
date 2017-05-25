package org.anddev.andengine.opengl.texture;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.opengl.texture.Texture.TextureSourceWithLocation;
import org.anddev.andengine.opengl.texture.source.ITextureSource;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

/**
 * @author Nicolas Gramlich
 * @since 13:00:09 - 05.04.2010
 */
public class TextureOptions {
	// ===========================================================
	// Constants
	// ===========================================================

	public static final TextureOptions DEFAULT = new TextureOptions(GL10.GL_NEAREST, GL10.GL_NEAREST, GL10.GL_CLAMP_TO_EDGE, GL10.GL_CLAMP_TO_EDGE, GL10.GL_MODULATE);
	public static final TextureOptions BILINEAR = new TextureOptions(GL10.GL_LINEAR, GL10.GL_LINEAR, GL10.GL_CLAMP_TO_EDGE, GL10.GL_CLAMP_TO_EDGE, GL10.GL_MODULATE);
	public static final TextureOptions REPEATING = new TextureOptions(GL10.GL_NEAREST, GL10.GL_NEAREST, GL10.GL_REPEAT, GL10.GL_REPEAT, GL10.GL_MODULATE);
	public static final TextureOptions REPEATING_BILINEAR = new TextureOptions(GL10.GL_LINEAR, GL10.GL_LINEAR, GL10.GL_REPEAT, GL10.GL_REPEAT, GL10.GL_MODULATE);

	// ===========================================================
	// Fields
	// ===========================================================

	public final int mMagFilter;
	public final int mMinFilter;
	public final float mWrapT;
	public final float mWrapS;
	public final int mTextureEnvironment;

	// ===========================================================
	// Constructors
	// ===========================================================

	public TextureOptions(final int pMinFilter, final int pMagFilter, final int pWrapT, final int pWrapS, final int pTextureEnvironment) {
		this.mMinFilter = pMinFilter;
		this.mMagFilter = pMagFilter;
		this.mWrapT = pWrapT;
		this.mWrapS = pWrapS;
		this.mTextureEnvironment = pTextureEnvironment;
	}

	public void addTextureSource(Texture texture, final ITextureSource pTextureSource, final int pTexturePositionX, final int pTexturePositionY) {
		texture.mTextureSources.add(new TextureSourceWithLocation(pTextureSource, pTexturePositionX, pTexturePositionY));
		texture.mUpdateOnHardwareNeeded = true;
	}

	void sendPlaceholderBitmapToHardware(final int pWidth, final int pHeight) {
		final Bitmap textureBitmap = Bitmap.createBitmap(pWidth, pHeight, Bitmap.Config.ARGB_8888);
	
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, textureBitmap, 0);
	
		textureBitmap.recycle();
	}

	void applyTextureOptions(Texture texture, final GL10 pGL) {
		final TextureOptions textureOptions = this;
		pGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, textureOptions.mMinFilter);
		pGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, textureOptions.mMagFilter);
		pGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, textureOptions.mWrapS);
		pGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, textureOptions.mWrapT);
		pGL.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, textureOptions.mTextureEnvironment);
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
