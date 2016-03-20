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
package org.mixare.gui;

import org.mixare.MarkerRenderer;
import org.mixare.data.DataHandler;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenObj;
import org.mixare.lib.marker.Marker;

/**
 * Takes care of the points on the small radar in the top left corner
 *
 * @author daniele
 */
public class RadarPoints implements ScreenObj {

    //Radius in pixel on screen
    private static float RADIUS = 40;

    //The renderer
    private MarkerRenderer markerRenderer;

    // The radar's range
    private float range;

    public RadarPoints(MarkerRenderer markerRenderer) {
        this.markerRenderer = markerRenderer;
    }

    public void paint(PaintScreen paintScreen) {
        /** radius is in KM. */
        range = markerRenderer.getRadius() * 1000;

        /** put the markers in it */
        float scale = range / RADIUS;

        DataHandler dataHandler = markerRenderer.getDataHandler();

        for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
            Marker marker = dataHandler.getMarker(i);
            float x = marker.getLocationVector().x / scale;
            float y = marker.getLocationVector().z / scale;

            if (marker.isActive() && (x * x + y * y < RADIUS * RADIUS)) {
                paintScreen.setFill(true);

                // For OpenStreetMap the color is changing based on the URL
                paintScreen.setColor(marker.getColor());

                paintScreen.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, 2);
            }
        }
    }


    //Width on screen
    public float getWidth() {
        return RADIUS * 2;
    }

    //Height on screen
    public float getHeight() {
        return RADIUS * 2;
    }
}