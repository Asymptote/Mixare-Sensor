/*
 * Copyright (C) 2010- Peer internet solutions
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

import org.mixare.MixViewActivity;
import org.mixare.lib.gui.PaintScreen;

import android.location.Location;

/**
 * The SocialMarker class represents a marker, which contains data from
 * sources like twitter etc. Social markers appear at the top of the screen
 * and show a small logo of the source.
 * 
 * @author hannes
 *
 */
public class SocialMarker extends LocalMarker {
	
	public static final int MAX_OBJECTS=15;

	public SocialMarker(String id, String title, double latitude, double longitude,
			double altitude, String URL, int type, int color) {
		super(id, title, latitude, longitude, altitude, URL, type, color);
	}

	@Override
	public void update(Location curGPSFix) {
		// we want the social markers to be on the upper part of
		// your surrounding sphere 
		double altitude = curGPSFix.getAltitude()+Math.sin(0.35)*distance+Math.sin(0.4)*(distance/(MixViewActivity.getMarkerRendererStatically().getRadius()*1000f/distance));
		getGeoLocation().setAltitude(altitude);

		super.update(curGPSFix);
	}
	
	@Override
	public void draw(PaintScreen paintScreen) {
		drawTextBlock(paintScreen);
		drawCircle(paintScreen);
	}	
	
	@Override
	public void drawCircle(PaintScreen paintScreen) {
		if (isVisible) {
			float maxHeight = paintScreen.getHeight();
			paintScreen.setStrokeWidth(maxHeight / 100f);
			paintScreen.setFill(false);

			paintScreen.setColor(getColor());
			
			// draw circle with radius depending on distance
			// 0.44 is approx. vertical fov in radians
			double angle = 2.0 * Math.atan2(10, distance);
			double radius = Math.max(
					Math.min(angle / 0.44 * maxHeight, maxHeight),
					maxHeight / 25f);

			/*
			 * distance 100 is the threshold to convert from circle to another
			 * shape
			 */
			paintScreen.paintCircle(cMarker.x, cMarker.y, (float) radius);

		}
	}
	
	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}
}