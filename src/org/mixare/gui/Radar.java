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
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenObj;
import org.mixare.lib.render.Camera;

import android.graphics.Color;

/** Takes care of the small radar in the top left corner and of its points
 * @author daniele
 *
 */
public class Radar implements ScreenObj {
    /** current context */
    private MixContext mixContext;
	/** The screen */
	private MarkerRenderer markerRenderer;

    private RadarPoints radarPoints = null;

    private PaintScreen paintScreen = null;
	/** The radar's range */
	float range;
	/** Radius in pixel on screen */
	public static float RADIUS = 40;
	/** Position on screen */
	private static float originX = 10 , originY = 20;
	/** Color */
	private static int radarColor = Color.argb(100, 0, 0, 200);

    private ScreenLine lrl = new ScreenLine();
    private ScreenLine rrl = new ScreenLine();

    private String[] directions;

    private float rx = 10, ry = 20;

    public Radar(){
        this.markerRenderer = MixContext.getInstance().getActualMixViewActivity().getMarkerRenderer();
        mixContext= MixContext.getInstance();
        directions = new String[8];
        directions[0] = mixContext.getString(R.string.N);
        directions[1] = mixContext.getString(R.string.NE);
        directions[2] = mixContext.getString(R.string.E);
        directions[3] = mixContext.getString(R.string.SE);
        directions[4] = mixContext.getString(R.string.S);
        directions[5] = mixContext.getString(R.string.SW);
        directions[6] = mixContext.getString(R.string.W);
        directions[7] = mixContext.getString(R.string.NW);
        radarPoints = new RadarPoints(this.markerRenderer);

    }

    public void paint(PaintScreen paintScreen) {
        this.paintScreen = paintScreen;
		/** radius is in KM. */
		range = markerRenderer.getRadius() * 1000;
        int bearing = (int) markerRenderer.getState().getCurBearing();

        /** Draw the radar */
		this.paintScreen.setFill(true);
		this.paintScreen.setColor(radarColor);
		this.paintScreen.paintCircle(originX + RADIUS, originY + RADIUS, RADIUS);

		/** put the markers in it */
        this.paintScreen.paintObj(radarPoints, rx, ry, -bearing, 1);


        String dirTxt = "";
        int range = (int) (bearing / (360f / 16f));
        if (range == 15 || range == 0)
            dirTxt = directions[0];
        else if (range == 1 || range == 2)
            dirTxt = directions[1];
        else if (range == 3 || range == 4)
            dirTxt = directions[2];
        else if (range == 5 || range == 6)
            dirTxt = directions[3];
        else if (range == 7 || range == 8)
            dirTxt = directions[4];
        else if (range == 9 || range == 10)
            dirTxt = directions[5];
        else if (range == 11 || range == 12)
            dirTxt = directions[6];
        else if (range == 13 || range == 14)
            dirTxt = directions[7];


 //       paintScreen.paintObj(this, rx, ry, -bearing, 1);
        this.paintScreen.setFill(false);
        this.paintScreen.setColor(Color.argb(150, 0, 0, 220));
        this.paintScreen.paintLine(lrl.x, lrl.y, rx + RADIUS, ry
                + RADIUS);
        this.paintScreen.paintLine(rrl.x, rrl.y, rx + RADIUS, ry
                + RADIUS);
        this.paintScreen.setColor(Color.rgb(255, 255, 255));
        this.paintScreen.setFontSize(12);

        radarText(MixUtils.formatDist(markerRenderer.getRadius() * 1000), rx
                + RADIUS, ry + RADIUS * 2 - 10, false); //show range
        radarText("" + bearing + ((char) 176) + " " + dirTxt, rx
                + RADIUS, ry - 5, true); //show bearing

        lrl.set(0, -RADIUS);
        lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
        lrl.add(rx + RADIUS, ry + RADIUS);
        rrl.set(0, -RADIUS);
        rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
        rrl.add(rx + RADIUS, ry + RADIUS);
	}

    private void radarText(String txt, float x, float y,
                           boolean bg) {
        float padw = 4, padh = 2;
        float w = paintScreen.getTextWidth(txt) + padw * 2;
        float h = paintScreen.getTextAsc() + paintScreen.getTextDesc() + padh * 2;
        if (bg) {
            paintScreen.setColor(Color.rgb(0, 0, 0));
            paintScreen.setFill(true);
            paintScreen.paintRect(x - w / 2, y - h / 2, w, h);
            paintScreen.setColor(Color.rgb(255, 255, 255));
            paintScreen.setFill(false);
            paintScreen.paintRect(x - w / 2, y - h / 2, w, h);
        }
        paintScreen.paintText(padw + x - w / 2, padh + paintScreen.getTextAsc() + y - h / 2, txt,
                false);
    }



	/** Width on screen */
	public float getWidth() {
		return RADIUS * 2;
	}

	/** Height on screen */
	public float getHeight() {
		return RADIUS * 2;
	}
}

