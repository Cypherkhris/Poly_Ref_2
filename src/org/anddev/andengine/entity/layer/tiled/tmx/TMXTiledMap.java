package org.anddev.andengine.entity.layer.tiled.tmx;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.microedition.khronos.opengles.GL11;

import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.anddev.andengine.entity.layer.tiled.tmx.util.constants.TMXConstants;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.vertex.RectangleVertexBuffer;
import org.anddev.andengine.util.Base64;
import org.anddev.andengine.util.Base64InputStream;
import org.anddev.andengine.util.SAXUtils;
import org.anddev.andengine.util.StreamUtils;
import org.xml.sax.Attributes;

import android.util.SparseArray;

/**
 * @author Nicolas Gramlich
 * @since 19:38:11 - 20.07.2010
 */
public class TMXTiledMap implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final String mOrientation;
	private final int mTilesHorizontal;
	private final int mTilesVertical;
	private final int mTileWidth;
	private final int mTileHeight;

	final ArrayList<TMXTileSet> mTMXTileSets = new ArrayList<TMXTileSet>();
	final ArrayList<TMXLayer> mTMXLayers = new ArrayList<TMXLayer>();
	private final ArrayList<TMXObjectGroup> mTMXObjectGroups = new ArrayList<TMXObjectGroup>();

	private final RectangleVertexBuffer mSharedVertexBuffer;

	private final SparseArray<TextureRegion> mGlobalTileIDToTextureRegionCache = new SparseArray<TextureRegion>();
	private final SparseArray<ArrayList<TMXTileProperty>> mGlobalTileIDToTMXTilePropertiesCache = new SparseArray<ArrayList<TMXTileProperty>>();

	// ===========================================================
	// Constructors
	// ===========================================================

	TMXTiledMap(final Attributes pAttributes) {
		this.mOrientation = pAttributes.getValue("", TAG_MAP_ATTRIBUTE_ORIENTATION);
		if(this.mOrientation.equals(TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL) == false) {
			throw new IllegalArgumentException(TAG_MAP_ATTRIBUTE_ORIENTATION + ": '" + this.mOrientation + "' is not supported.");
		}
		this.mTilesHorizontal = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_MAP_ATTRIBUTE_WIDTH);
		this.mTilesVertical = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_MAP_ATTRIBUTE_HEIGHT);
		this.mTileWidth = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_MAP_ATTRIBUTE_TILEWIDTH);
		this.mTileHeight = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_MAP_ATTRIBUTE_TILEHEIGHT);

		this.mSharedVertexBuffer = new RectangleVertexBuffer(GL11.GL_STATIC_DRAW);
		BufferObjectManager.getActiveInstance().loadBufferObject(this.mSharedVertexBuffer);
		this.mSharedVertexBuffer.onUpdate(0, 0, this.mTileWidth, this.mTileHeight);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public final String getOrientation() {
		return this.mOrientation;
	}

	public final int getWidth() {
		return this.mTilesHorizontal;
	}

	public final int getHeight() {
		return this.mTilesVertical;
	}

	public final int getTileWidth() {
		return this.mTileWidth;
	}

	public final int getTileHeight() {
		return this.mTileHeight;
	}

	public RectangleVertexBuffer getSharedVertexBuffer() {
		return this.mSharedVertexBuffer;
	}

	public ArrayList<TMXTileSet> getTMXTileSets() {
		return this.mTMXTileSets;
	}

	public ArrayList<TMXLayer> getTMXLayers() {
		return this.mTMXLayers;
	}

	void addTMXObjectGroup(final TMXObjectGroup pTMXObjectGroup) {
		this.mTMXObjectGroups.add(pTMXObjectGroup);
	}

	public ArrayList<TMXObjectGroup> getTMXObjectGroups() {
		return this.mTMXObjectGroups;
	}

	public ArrayList<TMXTileProperty> getTMXTilePropertiesByGlobalTileID(final int pGlobalTileID) {
		return this.mGlobalTileIDToTMXTilePropertiesCache.get(pGlobalTileID);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public ArrayList<TMXTileProperty> getTMXTileProperties(final int pGlobalTileID) {
		final SparseArray<ArrayList<TMXTileProperty>> globalTileIDToTMXTilePropertiesCache = this.mGlobalTileIDToTMXTilePropertiesCache;

		final ArrayList<TMXTileProperty> cachedTMXTileProperties = globalTileIDToTMXTilePropertiesCache.get(pGlobalTileID);
		if(cachedTMXTileProperties != null) {
			return cachedTMXTileProperties;
		} else {
			final ArrayList<TMXTileSet> tmxTileSets = this.mTMXTileSets;

			for(int i = tmxTileSets.size() - 1; i >= 0; i--) {
				final TMXTileSet tmxTileSet = tmxTileSets.get(i);
				if(pGlobalTileID >= tmxTileSet.getFirstGlobalTileID()) {
					return tmxTileSet.getTMXTilePropertiesFromGlobalTileID(pGlobalTileID);
				}
			}
			throw new IllegalArgumentException("No TMXTileProperties found for pGlobalTileID=" + pGlobalTileID);
		}
	}

	public TextureRegion getTextureRegionFromGlobalTileID(final int pGlobalTileID) {
		final SparseArray<TextureRegion> globalTileIDToTextureRegionCache = this.mGlobalTileIDToTextureRegionCache;

		final TextureRegion cachedTextureRegion = globalTileIDToTextureRegionCache.get(pGlobalTileID);
		if(cachedTextureRegion != null) {
			return cachedTextureRegion;
		} else {
			final ArrayList<TMXTileSet> tmxTileSets = this.mTMXTileSets;

			for(int i = tmxTileSets.size() - 1; i >= 0; i--) {
				final TMXTileSet tmxTileSet = tmxTileSets.get(i);
				if(pGlobalTileID >= tmxTileSet.getFirstGlobalTileID()) {
					final TextureRegion textureRegion = tmxTileSet.getTextureRegionFromGlobalTileID(pGlobalTileID);
					/* Add to cache for the all future pGlobalTileIDs with the same value. */
					globalTileIDToTextureRegionCache.put(pGlobalTileID, textureRegion);
					return textureRegion;
				}
			}
			throw new IllegalArgumentException("No TextureRegion found for pGlobalTileID=" + pGlobalTileID);
		}
	}

	public void initializeTMXTiles(TMXLayer tmxLayer, final String pDataString, final ITMXTilePropertiesListener pTMXTilePropertyListener) throws IOException, IllegalArgumentException {
		final TMXTiledMap tmxTiledMap = this;
		final int tileWidth = getTileWidth();
		final int tileHeight = getTileHeight();
	
		final int tilesHorizontal = tmxLayer.mTileColumns;
		final int tilesVertical = tmxLayer.mTileRows;
	
		final TMXTile[][] tmxTiles = tmxLayer.mTMXTiles;
	
		final int globalTileIDsExpected = tilesHorizontal * tilesVertical;
	
		DataInputStream dataIn = null;
		try{
			dataIn = new DataInputStream(new GZIPInputStream(new Base64InputStream(new ByteArrayInputStream(pDataString.getBytes("UTF-8")), Base64.DEFAULT)));
	
			int globalTileIDsRead = 0;
			while(globalTileIDsRead < globalTileIDsExpected) {
				final int globalTileID = tmxLayer.mTMXTiledMap.readGlobalTileID(dataIn);
	
				final int column = globalTileIDsRead % tilesHorizontal;
				final int row = globalTileIDsRead / tilesHorizontal;
	
				final TextureRegion tmxTileTextureRegion;
				if(globalTileID == 0) {
					tmxTileTextureRegion = null;
				} else {
					tmxTileTextureRegion = tmxTiledMap.getTextureRegionFromGlobalTileID(globalTileID);
				}
				final TMXTile tmxTile = new TMXTile(globalTileID, row, column, tileWidth, tileHeight, tmxTileTextureRegion);
				tmxTiles[row][column] = tmxTile;
	
				if(globalTileID != 0) {
					/* Notify the ITMXTilePropertiesListener if it exists. */
					if(pTMXTilePropertyListener != null) {
						final ArrayList<TMXTileProperty> tmxTileProperties = tmxTiledMap.getTMXTileProperties(globalTileID);
						if(tmxTileProperties != null) {
							pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(tmxTiledMap, tmxLayer, tmxTile, tmxTileProperties);
						}
					}
				}
	
				globalTileIDsRead++;
			}
		} finally {
			StreamUtils.closeStream(dataIn);
		}
	}

	int readGlobalTileID(final DataInputStream pDataIn) throws IOException {
		final int lowestByte = pDataIn.read();
		final int secondLowestByte = pDataIn.read();
		final int secondHighestByte = pDataIn.read();
		final int highestByte = pDataIn.read();
	
		if(lowestByte < 0 || secondLowestByte < 0 || secondHighestByte < 0 || highestByte < 0) {
			throw new IllegalArgumentException("Couldn't read global Tile ID.");
		}
	
		return lowestByte | secondLowestByte <<  8 |secondHighestByte << 16 | highestByte << 24;
	}
	
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
