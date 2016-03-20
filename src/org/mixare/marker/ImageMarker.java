/*
 * Copyright (C) 2012 
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.marker;

import java.io.IOException;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.marker.draw.DrawImage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;



/**
 * Local Image Marker that handles drawing images locally 
 * (extents {@link org.mixare.marker.LocalMarker LocalMarker})
 * 
 * Note: LinkURL is the url when marker is clicked on.
 * Note: ImageURL is the url that links to solid image.
 * 
 * @author devBinnooh
 * @author A.Egal
 */
public class ImageMarker extends LocalMarker {

	/** Int MaxObjects that can be create of this marker */
	public static final int maxObjects = 30;
	/** BitMap Image storage */
	private Bitmap image;
	
	/**
	 * Constructor with the given params.
	 * Note Image will be set to default (empty square).
	 * Please Set the image {@link org.mixare.marker.ImageMarker#setImage(Bitmap)},
	 * or Call this class with Image URL.
	 * 
	 * @see org.mixare.marker.ImageMarker#ImageMarker(String, String, double, double, double, String, int, int, String, String)
	 * @param id Marker's id
	 * @param title Marker's title
	 * @param latitude latitude
	 * @param longitude longitude
	 * @param altitude altitude
	 * @param link link
	 * @param type Datasource type
	 * @param color Color int representation {@link android.graphics.Color Color}
	 */
	public ImageMarker(String id, String title, double latitude,
			double longitude, double altitude, String link, int type, int color) {
		super(id, title, latitude, longitude, altitude, link, type, color);
		this.setImage(Bitmap.createBitmap(10, 10, Config.ARGB_4444)); //TODO set default Image if image not Available
	}
	
	/**
	 * Constructor with the given params.
	 * Marker will handle retrieving the image from Image URL,
	 * Please ensure that it links to an Image.
	 * 
	 * @param id Marker's id
	 * @param title Marker's title
	 * @param latitude latitude
	 * @param longitude longitude
	 * @param altitude altitude
	 * @param pageLink link
	 * @param type Datasource type
	 * @param color Color int representation {@link android.graphics.Color Color}
	 * @param imageOwner ImageOwner's name
	 * @param ImageUrl Image's url
	 */
	public ImageMarker (String id, String title, double latitude,
			double longitude, double altitude, final String pageLink, 
			final int type, final int color,final String imageOwner,
			final String ImageUrl) {
		super(id, title, latitude, longitude, altitude, pageLink, type, color);
		
		try {
			final java.net.URL imageURI = new java.net.URL (ImageUrl);
			this.setImage(BitmapFactory.decodeStream(imageURI.openConnection().getInputStream()));
		} catch (IOException e) {  //also catches MalformedURLException
			Log.e(org.mixare.Config.TAG, e.getMessage());
		}finally {
			if (null == this.getImage()){
				this.setImage(Bitmap.createBitmap(10, 10, Config.ARGB_4444));
			}
		}
	}
	
	/**
	 * Image Marker Draw Function.
	 * {@inheritDoc}
	 */
	public void draw(final PaintScreen paintScreen){
		drawImage(paintScreen);
		drawTitle(paintScreen);
	}

	/**
	 * Draw a title for image. It displays full title if title's length is less
	 * than 10 chars, otherwise, it displays the first 10 chars and concatenate
	 * three dots "..."
	 * 
	 * @param paintScreen View Screen that title screen will be drawn into
	 */
	public void drawTitle(final PaintScreen paintScreen) {
		if (isVisible) {
			final float maxHeight = Math.round(paintScreen.getHeight() / 10f) + 1;
			String textStr = MixUtils.shortenTitle(title,distance);
			textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
					paintScreen, underline);
			 paintScreen.setColor(this.getColor());
			final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
					getSignMarker().x, getSignMarker().y);
			txtLab.prepare(textBlock);
			paintScreen.setStrokeWidth(1f);
			paintScreen.setFill(true);
			paintScreen.paintObj(txtLab, getSignMarker().x - txtLab.getWidth() / 2,
					getSignMarker().y + maxHeight, currentAngle + 90, 1);
		}
	}
	
	/**
	 * Handles Drawing Images
	 * @param paintScreen Screen that Image will be drawn into
	 */
	public void drawImage(final PaintScreen paintScreen) {
		final DrawImage Image = new DrawImage(isVisible, cMarker, image);
		Image.draw(paintScreen);
	}
	

	private MixVector getSignMarker() {
		return this.signMarker;
	}

	@Override
	public int getMaxObjects() {
		return maxObjects;
	}

	/**
	 * @return Bitmap image
	 */
	public Bitmap getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Bitmap image) {
		this.image = image;
	}


    @Override
	public void update(Location curGPSFix) {
		super.update(curGPSFix);
	}
}
