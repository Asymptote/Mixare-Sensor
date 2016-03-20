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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mixare.Config;
import org.mixare.MarkerListFragment;
import org.mixare.MixContext;
import org.mixare.MixState;
import org.mixare.MixViewDataHolder;
import org.mixare.R;
import org.mixare.data.convert.Elevation;
import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.reality.PhysicalPlace;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.map.MixMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

/**
 * The class represents a marker and contains its information.
 * It draws the marker itself and the corresponding label.
 * All markers are specific markers like SocialMarkers or
 * NavigationMarkers, since this class is abstract
 */

public abstract class LocalMarker implements Marker {

    private static final String INTENT_EXTRA_MENUENTRY = "menuentry";
    private static final String INTENT_EXTRA_LONGITUDE = "longitude";
    private static final String INTENT_EXTRA_LATITUDE = "latitude";
    private static final String INTENT_EXTRA_DO_CENTER = "do_center";
    private String ID;
	protected String title;
	protected boolean underline = false;
	private String URL;
	protected PhysicalPlace geoLocation;
	/* distance from user to geoLocation in meters */
	protected double distance;
	/* Marker's color */
	private int color;
	
	private boolean active;

	// Draw properties
	/* Marker's Visibility to user */
	protected boolean isVisible;
//	private boolean isLookingAt;
//	private boolean isNear;
//	private float deltaCenter;
	public MixVector cMarker = new MixVector();  //clickMarker?
	
	protected MixVector signMarker = new MixVector();

	protected MixVector locationVector = new MixVector();
	
	private MixVector origin = new MixVector(0, 0, 0);
	
	private MixVector upV = new MixVector(0, 1, 0);
	
	private ScreenLine pPt = new ScreenLine();

	public Label txtLab = new Label();
	
	protected TextObj textBlock;

	public LocalMarker(final String id,  String title, final double latitude,
			 double longitude, final double altitude,final String link,
			int type, final int color) {
		super();

		this.active = false;
		this.title = title;
		this.geoLocation = (new PhysicalPlace(latitude,longitude,altitude));
		if (link != null && link.length() > 0) {
			try {
				this.URL = (MixUtils.URL_PREFIX_WEBPAGE + MixUtils.URL_PREFIX_SEPARATOR + URLDecoder.decode(link, MixUtils.CHARSET_NAME_UTF_8));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.underline = true;
		}
		this.color = color;
		this.ID = id + "##" + type + "##" + title;
	}


	private void cCMarker(MixVector originalPoint, Camera viewCam, float addX, float addY) {

		// Temp properties
		final MixVector tmpa = new MixVector(originalPoint);
		final MixVector tmpc = new MixVector(upV);
		tmpa.add(locationVector); //3 
		tmpc.add(locationVector); //3
		tmpa.sub(viewCam.lco); //4
		tmpc.sub(viewCam.lco); //4
		tmpa.prod(viewCam.transform); //5
		tmpc.prod(viewCam.transform); //5

		final MixVector tmpb = new MixVector();
		viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
		cMarker.set(tmpb); //7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
		signMarker.set(tmpb); //7
	}

	/**
	 * Checks if Marker is within Z angle of Camera.
	 * It sets the visibility upon that.
	 */
	private void calcVisibility() {
		isVisible = false;
//		isLookingAt = false;
//		deltaCenter = Float.MAX_VALUE;

		if (cMarker.z < -1f) {
			isVisible = true;
		}
	}

	public void update(Location curGPSFix) {
		// Checks if program should get altitude from http://api.geonames.org/astergdem
        if (this.getURL() != null && this.getGeoLocation().getAltitude() == 0.0) {
            this.getGeoLocation().setAltitude(
                    Elevation.getElevation().lookupElevation(
                            curGPSFix.getLatitude(),
                            curGPSFix.getLongitude()));
        }

		// compute the relative position vector from user position to POI location
        locationVector.calculateRelative(curGPSFix, getGeoLocation());
	}

	public void calcPaint(Camera viewCam, float addX, float addY) {
		cCMarker(origin, viewCam, addX, addY);
		calcVisibility();
	}

//	private void calcPaint(Camera viewCam) {
//		cCMarker(origin, viewCam, 0, 0);
//	}

	public boolean isClickValid(float x, float y) {
		
		//if the marker is not active (i.e. not shown in AR markerRenderer) we don't have to check it for clicks
		if (!isActive() && !this.isVisible)
			return false;

		final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);
		//TODO adapt the following to the variable radius!
		pPt.x = x - signMarker.x;
		pPt.y = y - signMarker.y;
		pPt.rotate((float) Math.toRadians(-(currentAngle + 90)));
		pPt.x += txtLab.getX();
		pPt.y += txtLab.getY();

		final float objX = txtLab.getX() - txtLab.getWidth() / 2;
		float objY = txtLab.getY() - txtLab.getHeight() / 2;
		float objW = txtLab.getWidth();
		float objH = txtLab.getHeight();

		if (pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
				&& pPt.y < objY + objH) {
			return true;
		} else {
			return false;
		}
	}

	public void draw(PaintScreen paintScreen) {
		drawCircle(paintScreen);
		if (Config.drawTextBlock) {
			drawTextBlock(paintScreen);
		}
	}

	public void drawCircle(PaintScreen paintScreen) {

		if (isVisible) {
			//float maxHeight = Math.round(paintScreen.getHeight() / 10f) + 1;
			float maxHeight = paintScreen.getHeight();
			paintScreen.setStrokeWidth(maxHeight / 100f);
			paintScreen.setFill(false);
			//paintScreen.setColor(DataSource.getColor(type));

			//draw circle with radius depending on distance
			//0.44 is approx. vertical fov in radians 
			double angle = 2.0*Math.atan2(10,distance);
			double radius = Math.max(Math.min(angle/0.44 * maxHeight, maxHeight),maxHeight/25f);
			//double radius = angle/0.44d * (double)maxHeight;

			paintScreen.paintCircle(cMarker.x, cMarker.y, (float) radius);
		}
	}

	public void drawTextBlock(PaintScreen paintScreen) {
		//TODO: grandezza cerchi e trasparenza
		float maxHeight = Math.round(paintScreen.getHeight() / 10f) + 1;

		//TODO: change textblock only when distance changes
		String textStr="";

		double d = distance;
		DecimalFormat df = new DecimalFormat("@#");
		if(d<1000.0) {
			textStr = getTitle() + " ("+ df.format(d) + "m)";			
		}
		else {
			d=d/1000.0;
			textStr = getTitle() + " (" + df.format(d) + "km)";
		}

		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1,
				250, paintScreen, isUnderline());

		if (isVisible) {

			//paintScreen.setColor(DataSource.getColor(type));

			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);

			txtLab.prepare(textBlock);

			paintScreen.setStrokeWidth(1f);
			paintScreen.setFill(true);
			paintScreen.paintObj(txtLab, signMarker.x - txtLab.getWidth()
                    / 2, signMarker.y + maxHeight, currentAngle + 90, 1);
		}

	}

	public boolean doClick(float x, float y, MixContextInterface ctxI, MixStateInterface stateI)  {
		boolean evtHandled = false;

		if (isClickValid(x, y)) {
			if (getURL() != null) {
                MixContext ctx = MixContext.getInstance();
                if(! (stateI instanceof MixState)){
                    return false;
                }
                MixState state = (MixState) stateI;
                String onPress=getURL();

                if (onPress != null && onPress.startsWith(MixUtils.URL_PREFIX_WEBPAGE)) {
                    String webpage = MixUtils.parseAction(onPress);
                    try {
                        ctx.getWebContentManager().loadWebPage(webpage, ctx.getActualMixViewActivity());
                        state.setDetailsView(true);
                        evtHandled=true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
			}
		}
		return evtHandled;
	}

    public PopupMenu retrieveActionPopupMenu(final Context ctx, final View v){
        PopupMenu popup = new PopupMenu(ctx,v);

        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            Intent popupAction = null;

            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                boolean eventHandled;

                switch (menuItem.getItemId()){
                    case R.id.menuitem_show_on_map:
                        popupAction = prepareAction(ctx, MixMap.class, R.string.marker_action_show_on_map);
                        popupAction.putExtra(INTENT_EXTRA_DO_CENTER, true);
                        eventHandled=true;
                        break;
                    case R.id.menuitem_set_as_location:
                        eventHandled=true;
                        break;
                    case R.id.menuitem_set_as_destination:
                        eventHandled=true;
                        break;
                    case R.id.menuitem_show_website:
                        eventHandled=true;
                        break;
                    case R.id.menuitem_show_website_external:
                        eventHandled=true;
                        break;
                    case R.id.menuitem_show_details:
                        eventHandled=true;
                        break;
                    case R.id.menuitem_show_image:
                        eventHandled=true;
                        break;
                    case R.id.menuitem_start_routing:
                        Location destination=Config.getManualFix();
                        destination.setLatitude(LocalMarker.this.getLatitude());
                        destination.setLongitude(LocalMarker.this.getLongitude());
                        MixViewDataHolder.getInstance().setCurDestination(destination);

                        popupAction = prepareAction(ctx, MixMap.class,R.string.marker_action_start_routing);
                        popupAction.putExtra(INTENT_EXTRA_DO_CENTER, true);
                        eventHandled=true;
                        break;
                    case R.id.menuitem_start_routing_external:
                        eventHandled=true;
                        break;
                    default:
                        eventHandled=false;
                }
                ctx.startActivity(popupAction);
                return eventHandled;
            }
        });
        inflater.inflate(R.menu.marker_actions, popup.getMenu());

        return popup;
    }

    public Intent prepareAction(Context ctx, Class clazz, int menuEntry){
        Intent intent = new Intent(ctx, clazz);
        intent.putExtra(INTENT_EXTRA_LATITUDE, this.getLatitude());
        intent.putExtra(INTENT_EXTRA_LONGITUDE, this.getLongitude());
        intent.putExtra(INTENT_EXTRA_MENUENTRY, menuEntry);
        return intent;
    }

	/* ****** Getters / setters **********/
	
	public String getTitle(){
		return title;
	}

	public String getURL(){
		return URL;
	}

	public double getLatitude() {
		return getGeoLocation().getLatitude();
	}

	public double getLongitude() {
		return getGeoLocation().getLongitude();
	}

	public double getAltitude() {
		return getGeoLocation().getAltitude();
	}

	public MixVector getLocationVector() {
		return locationVector;
	}
	
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void setAltitude(double altitude) {
		getGeoLocation().setAltitude(altitude);
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public int compareTo(Marker another) {

		Marker leftPm = this;
		Marker rightPm = another;

		return Double.compare(leftPm.getDistance(), rightPm.getDistance());

	}

	@Override
	public boolean equals (Object marker) {
		return this.ID.equals(((Marker) marker).getID());
	}
	
	@Override
	public int hashCode() {
		return this.ID.hashCode();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	abstract public int getMaxObjects();
	
	//abstract maybe!!
	public void setImage(Bitmap image){
	}
	//Abstract!!
	public Bitmap getImage(){
		return null;
	}

	//get Color for OpenStreetMap based on the URL number
	public int getColor() {
		return color;
	}
	
	@Override
	public void setTxtLab(Label txtLab) {
		this.txtLab = txtLab;
	}

	@Override
	public Label getTxtLab() {
		return txtLab;
	}
	
	public void setExtras(String name, PrimitiveProperty primitiveProperty){
		//nothing to add
	}

	public void setExtras(String name, ParcelableProperty parcelableProperty){
		//nothing to add
	}


	/**
	 * @param title the title to set
	 */
	protected void setTitle(String title) {
		this.title = title;
	}


	/**
	 * @return the underline
	 */
	protected boolean isUnderline() {
		return underline;
	}


	/**
	 * @param underline the underline to set
	 */
	protected void setUnderline(boolean underline) {
		this.underline = underline;
	}


	/**
	 * @param uRL the uRL to set
	 */
	protected void setURL(String uRL) {
		URL = uRL;
	}


	/**
	 * @return the geoLocation
	 */
	protected PhysicalPlace getGeoLocation() {
		return geoLocation;
	}


	/**
	 * @param geoLocation the geoLocation to set
	 */
	protected void setGeoLocation(PhysicalPlace geoLocation) {
		this.geoLocation = geoLocation;
	}
}
