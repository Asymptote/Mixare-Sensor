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
package org.mixare;

import static android.view.KeyEvent.KEYCODE_CAMERA;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.Camera;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadRequest;
import org.mixare.mgr.downloader.DownloadResult;

import android.location.Location;
import android.util.Log;

/**
 * This class updates the markers. It also handles some user events
 * 
 * @author daniele
 * 
 */
public class MarkerRenderer {

	/** current context */
	private MixContext mixContext;
	/** is the markerRenderer Inited? */
	private boolean isInit;

	/** width and height of the markerRenderer */
	private int width, height;

	/**
	 * _NOT_ the android camera, the class that takes care of the transformation
	 */
	private Camera cam;

	private final MixState state = new MixState();

	/** The markerRenderer can be "frozen" for debug purposes */
	private boolean frozen;

	/** how many times to re-attempt download */
	private int retry;

	private Location curFix;
	private DataHandler dataHandler = new DataHandler();
	private float radius = 20;

	/** timer to refresh the browser */
	private Timer refreshTimer = null; // there is method called refresh, this
										// is different
	private final long refreshDelay = 45 * 1000; // refresh every 45 seconds

	private boolean isLauncherStarted;

	private final ArrayList<UIEvent> uiEvents = new ArrayList<>();

	private float addX = 0, addY = 0;

    private List<Marker> markers;

    public boolean dataSourceWorking;

    /**
	 * Constructor
	 */
	public MarkerRenderer(MixContext ctx) {
		this.mixContext = ctx;
	}

	public MixContext getContext() {
		return mixContext;
	}

	public boolean getIsLauncherStarted() {
		return isLauncherStarted;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(final boolean frozen) {
		this.frozen = frozen;
	}

	public float getRadius() {
        return radius;
	}

	public MixState getState (){
		return this.state;
	}

	public void setRadius(float radius) {
		this.radius = radius;
    }

	public DataHandler getDataHandler() {
		return dataHandler;
	}

	public boolean isDetailsView() {
		return state.isDetailsView();
	}

	public void setDetailsView(boolean detailsView) {
		state.setDetailsView(detailsView);
	}

	public void doStart() {
		state.nextLStatus = MixState.NOT_STARTED;
		mixContext.getLocationFinder().setLocationAtLastDownload(curFix);
	}

	public boolean isInited() {
		return isInit;
	}
    private PaintScreen paintScreen;

	public void init(PaintScreen paintScreen){
		this.paintScreen=paintScreen;
		try {
			width = paintScreen.getWidth();
			height = paintScreen.getHeight();

			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

		} catch (Exception ex) {
			// ex.printStackTrace();
			Log.e(Config.TAG, ex.getMessage());
		}
		frozen = false;
		isInit = true;
	}

	public void requestData(String url) {
		DownloadRequest request = new DownloadRequest(new DataSource(
				"LAUNCHER", url, DataSource.TYPE.MIXARE,
				DataSource.DISPLAY.CIRCLE_MARKER, true));
		mixContext.getDataSourceManager().setAllDataSourcesForLauncher(
				request.getSource());
		mixContext.getDownloadManager().submitJob(request);
		state.nextLStatus = MixState.PROCESSING;
	}

	// public void requestData(DataSource datasource, double lat, double lon,
	// double alt, float radius, String locale) {
	// DownloadRequest request = new DownloadRequest();
	// request.params = datasource.createRequestParams(lat, lon, alt, radius,
	// locale);
	// request.source = datasource;
	//
	// mixContext.getDownloadManager().submitJob(request);
	// state.nextLStatus = MixState.PROCESSING;
	// }

	public Camera getCam() {
		return cam;
	}

	public void draw() {
		mixContext.getRM(cam.transform);
		curFix = mixContext.getLocationFinder().getCurrentLocation();  // why get location on every draw cycle instead of only when it changed?
		MixViewDataHolder.getInstance().setCurLocation(curFix);

		state.calcPitchBearing(cam.transform);

		// Load Layer
		if (state.nextLStatus == MixState.NOT_STARTED && !frozen) {
			loadDrawLayer();
			markers = new ArrayList<>();
		} else if (state.nextLStatus == MixState.PROCESSING) {
			DownloadManager dm = mixContext.getDownloadManager();

			markers.addAll(downloadDrawResults(dm));
			
			if (dm.isDone()) {
              //  dataSourceWorking=false;
                mixContext.updateDataSourceStatus(false,false,null);
				retry = 0;
				state.nextLStatus = MixState.DONE;

				dataHandler = new DataHandler(); //why throw away the previous markers/DataHandler?
				dataHandler.addMarkers(markers);
				dataHandler.setCurLocation(curFix); // why call setCurLocation every draw cycle instead of only when it changed?

				if (refreshTimer == null) { // start the refresh timer if it is
											// null
					refreshTimer = new Timer(false);
					Date date = new Date(System.currentTimeMillis()
							+ refreshDelay);
					refreshTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							refresh();
						}
					}, date, refreshDelay);
				}
			} else {
               // dataSourceWorking=true;
                mixContext.updateDataSourceStatus(true,false,null);
                dataHandler.addMarkers(markers);
				dataHandler.setCurLocation(curFix);
			}

		} else {
           mixContext.updateDataSourceStatus(false,false,null);
        }

		// Update markers
		dataHandler.updateActivationStatus(mixContext);
		for (int i = dataHandler.getMarkerCount() - 1; i >= 0; i--) {
			final Marker ma = dataHandler.getMarker(i);
			// if (ma.isActive() && (ma.getDistance() / 1000f < radius || ma
			// instanceof NavigationMarker || ma instanceof SocialMarker)) {
			if (ma.isActive() && (ma.getDistance() / 1000f < radius)) {

				// To increase performance don't recalculate position vector
				// for every marker on every draw call, instead do this only
				// after setCurLocation and after downloading new marker
				// if (!frozen)
				// ma.update(curFix);
				if (!frozen) {
					ma.calcPaint(cam, addX, addY);
				}
				ma.draw(paintScreen);
			}
		}

        mixContext.getActualMixViewActivity().updateHud(curFix); //FIXME Problem: synchronized access prevents uiEvents from being processed

		// Get next event
		UIEvent evt = null;
		synchronized (uiEvents) {
			if (uiEvents.size() > 0) {
				evt = uiEvents.get(0);
				uiEvents.remove(0);
			}
		}
		if (evt != null) {
			switch (evt.type) {
			case UIEvent.KEY:
				handleKeyEvent((KeyEvent) evt);
				break;
			case UIEvent.CLICK:
				handleClickEvent((ClickEvent) evt);
				break;
			}
		}
		state.nextLStatus = MixState.PROCESSING;
	}



	/**
	 * Part of draw function, loads the layer.
	 */
	private void loadDrawLayer() {
		if (mixContext.getStartUrl().length() > 0) {
			requestData(mixContext.getStartUrl());
			isLauncherStarted = true;
		}

		else {
			double lat = curFix.getLatitude(), lon = curFix.getLongitude(), alt = curFix
					.getAltitude();
			state.nextLStatus = MixState.PROCESSING;
			mixContext.getDataSourceManager()
					.requestDataFromAllActiveDataSource(lat, lon, alt, radius);
		}

		// if no datasources are activated
		if (state.nextLStatus == MixState.NOT_STARTED) {
            state.nextLStatus = MixState.DONE;
        }
	}

	private List<Marker> downloadDrawResults(DownloadManager dm	) {
		DownloadResult dRes;
		List<Marker> markers = new ArrayList<>();
		while ((dRes = dm.getNextResult()) != null) {
			if (dRes.isError() && retry < 3) {
				retry++;
				mixContext.getDownloadManager().submitJob(
						dRes.getErrorRequest());
				// Notification
				// Toast.makeText(mixContext, dRes.errorMsg,
				// Toast.LENGTH_SHORT).show();
			}

			if (!dRes.isError()) {
				if (dRes.getMarkers() != null) {
					// jLayer = (DataHandler) dRes.obj;
					Log.i(Config.TAG, "Adding Markers");
					markers.addAll(dRes.getMarkers());

					// Notification
					mixContext.getNotificationManager().addNotification(
							mixContext.getResources().getString(
									R.string.download_received)
									+ " " + dRes.getDataSource().getName());
				}
			}
		}
		return markers;
	}

	private void handleKeyEvent(KeyEvent evt) {
		/** Adjust marker position with keypad */
		final float CONST = 10f;
		switch (evt.keyCode) {
		case KEYCODE_DPAD_LEFT:
			addX -= CONST;
			break;
		case KEYCODE_DPAD_RIGHT:
			addX += CONST;
			break;
		case KEYCODE_DPAD_DOWN:
			addY += CONST;
			break;
		case KEYCODE_DPAD_UP:
			addY -= CONST;
			break;
		case KEYCODE_DPAD_CENTER:
			frozen = !frozen;
			break;
		case KEYCODE_CAMERA:
			frozen = !frozen;
			break; // freeze the overlay with the camera button
		default: // if key is set, then ignore event
			break;
		}
	}

	boolean handleClickEvent(ClickEvent evt) {
		boolean evtHandled = false;

		// Handle event
		if (state.nextLStatus == MixState.DONE) {
			// the following will traverse the markers in ascending order (by
			// distance) the first marker that
			// matches triggers the event.
			// TODO handle collection of markers. (what if user wants the one at
			// the back)
			for (int i = 0; i < dataHandler.getMarkerCount() && !evtHandled; i++) {
				Marker pm = dataHandler.getMarker(i);

				evtHandled = pm.doClick(evt.x, evt.y, mixContext, state);
			}
		}
		return evtHandled;
	}



	public void clickEvent(float x, float y) {
		synchronized (uiEvents) {
			uiEvents.add(new ClickEvent(x, y));
		}
	}

	public void keyEvent(int keyCode) {
		synchronized (uiEvents) {
            uiEvents.add(new KeyEvent(keyCode));
		}
	}

	public void clearEvents() {
		synchronized (uiEvents) {
			uiEvents.clear();
		}
	}

	public void cancelRefreshTimer() {
		if (refreshTimer != null) {
			refreshTimer.cancel();
		}
	}

	/**
	 * Re-downloads the markers, and draw them on the map.
	 */
	public void refresh() {
		callRefreshToast();
		state.nextLStatus = MixState.NOT_STARTED;
	}

	private void callRefreshToast() {
		mixContext.getActualMixViewActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mixContext.getNotificationManager().addNotification(
                        mixContext.getString(R.string.refreshing));
			}
		});
	}



}

class UIEvent {
	public static final int CLICK = 0;
	public static final int KEY = 1;

	public int type;
}

class ClickEvent extends UIEvent {
	public float x, y;

	public ClickEvent(float x, float y) {
		this.type = CLICK;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}

class KeyEvent extends UIEvent {
	public int keyCode;

	public KeyEvent(int keyCode) {
		this.type = KEY;
		this.keyCode = keyCode;
	}

	@Override
	public String toString() {
		return "(" + keyCode + ")";
	}
}
