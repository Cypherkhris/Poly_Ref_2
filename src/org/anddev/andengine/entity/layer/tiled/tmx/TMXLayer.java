package org.anddev.andengine.entity.layer.tiled.tmx;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.collision.RectangularShapeCollisionChecker;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.layer.tiled.tmx.util.constants.TMXConstants;
import org.anddev.andengine.entity.shape.RectangularShape;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.util.GLHelper;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.SAXUtils;
import org.anddev.andengine.util.constants.Constants;
import org.xml.sax.Attributes;

/**
 * @author Nicolas Gramlich
 * @since 20:27:31 - 20.07.2010
 */
public class TMXLayer extends RectangularShape implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	final TMXTiledMap mTMXTiledMap;

	private final String mName;

	final int mTileColumns;
	final int mTileRows;

	final TMXTile[][] mTMXTiles;

	private final float[] mCullingVertices = new float[2 * RectangleVertexBuffer.VERTICES_PER_RECTANGLE];

	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXLayer(final TMXTiledMap pTMXTiledMap, final Attributes pAttributes) {
		super(0, 0, 0, 0, null);

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = pAttributes.getValue("", TAG_LAYER_ATTRIBUTE_NAME);
		this.mTileColumns = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_LAYER_ATTRIBUTE_WIDTH);
		this.mTileRows = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_LAYER_ATTRIBUTE_HEIGHT);
		this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];

		super.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		final float width = super.mWidth;
		super.mBaseWidth = width;

		super.mHeight = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		final float height = super.mHeight;
		super.mBaseHeight = height;

		this.mRotationCenterX = width * 0.5f;
		this.mRotationCenterY = height * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getName() {
		return this.mName;
	}

	public int getTileColumns() {
		return this.mTileColumns;
	}

	public int getTileRows() {
		return this.mTileRows;
	}

	public TMXTile[][] getTMXTiles() {
		return this.mTMXTiles;
	}

	public TMXTile getTMXTile(final int pRow, final int pColumn) throws ArrayIndexOutOfBoundsException {
		return this.mTMXTiles[pRow][pColumn];
	}

	/**
	 * @param pX in SceneCoordinates.
	 * @param pY in SceneCoordinates.
	 * @return the {@link TMXTile} located at <code>pX/pY</code>.
	 */
	public TMXTile getTMXTileAt(final float pX, final float pY) {
		final float[] localCoords = this.convertSceneToLocalCoordinates(pX, pY);
		final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;

		final int tileColumn = (int)(localCoords[Constants.VERTEX_INDEX_X] / tmxTiledMap.getTileWidth());
		if(tileColumn < 0 || tileColumn > this.mTileColumns - 1) {
			return null;
		}
		final int tileRow = (int)(localCoords[Constants.VERTEX_INDEX_Y] / tmxTiledMap.getTileWidth());
		if(tileRow < 0 || tileRow > this.mTileRows - 1) {
			return null;
		}

		return this.mTMXTiles[tileRow][tileColumn];
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	@Deprecated
	public void setRotation(final float pRotation) {

	}

	@Override
	protected void onUpdateVertexBuffer() {
		/* Nothing. */
	}

	@Override
	protected void onInitDraw(final GL10 pGL) {
		super.onInitDraw(pGL);

		GLHelper.enableTextures(pGL);
		GLHelper.enableTexCoordArray(pGL);
	}

	@Override
	protected void onApplyVertices(final GL10 pGL) {
		if(GLHelper.EXTENSIONS_VERTEXBUFFEROBJECTS) {
			final GL11 gl11 = (GL11)pGL;

			this.mTMXTiledMap.getSharedVertexBuffer().selectOnHardware(gl11);
			GLHelper.vertexZeroPointer(gl11);
		} else {
			GLHelper.vertexPointer(pGL, this.mTMXTiledMap.getSharedVertexBuffer().getFloatBuffer());
		}
	}

	@Override
	protected void drawVertices(final GL10 pGL, final Camera pCamera) {
		final TMXTile[][] tmxTiles = this.mTMXTiles;

		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int tileHeight = this.mTMXTiledMap.getTileHeight();

		final float scaledTileWidth = tileWidth * this.mScaleX;
		final float scaledTileHeight = tileHeight * this.mScaleY;

		final float[] cullingVertices = this.mCullingVertices;
		RectangularShapeCollisionChecker.fillVertices(this, cullingVertices);

		final float layerMinX = cullingVertices[0 + Constants.VERTEX_INDEX_X];
		final float layerMinY = cullingVertices[0 + Constants.VERTEX_INDEX_Y];

		final float cameraMinX = pCamera.getMinX();
		final float cameraMinY = pCamera.getMinY();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();

		/* Determine the area that is visible in the camera. */
		final float firstColumnRaw = (cameraMinX - layerMinX) / scaledTileWidth;
		final int firstColumn = MathUtils.bringToBounds(0, tileColumns - 1, (int)Math.floor(firstColumnRaw));
		final int lastColumn = MathUtils.bringToBounds(0, tileColumns - 1, (int)Math.ceil(firstColumnRaw + cameraWidth / scaledTileWidth));

		final float firstRowRaw = (cameraMinY - layerMinY) / scaledTileHeight;
		final int firstRow = MathUtils.bringToBounds(0, tileRows - 1, (int)Math.floor(firstRowRaw));
		final int lastRow = MathUtils.bringToBounds(0, tileRows - 1, (int)Math.floor(firstRowRaw + cameraHeight / scaledTileHeight));

		final int visibleTilesTotalWidth = (lastColumn - firstColumn + 1) * tileWidth;

		pGL.glTranslatef(firstColumn * tileWidth, firstRow * tileHeight, 0);

		for(int row = firstRow; row <= lastRow; row++) {
			final TMXTile[] tmxTileRow = tmxTiles[row];

			for(int column = firstColumn; column <= lastColumn; column++) {
				final TextureRegion textureRegion = tmxTileRow[column].mTextureRegion;
				if(textureRegion != null) {
					textureRegion.onApply(pGL);

					pGL.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
				}
				pGL.glTranslatef(tileWidth, 0, 0);
			}
			/* Translate one row downwards and the back left to the first column.
			 * Just like the 'Carriage Return' + 'New Line' (\r\n) on a typewriter. */
			pGL.glTranslatef(-visibleTilesTotalWidth, tileHeight, 0);
		}
		pGL.glLoadIdentity();
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		/* Nothing. */
	}

	void addTMXLayer(TMXTiledMap tmxTiledMap) {
		tmxTiledMap.mTMXLayers.add(this);
	}

	// ===========================================================
	// Methods
	// ===========================================================


	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
