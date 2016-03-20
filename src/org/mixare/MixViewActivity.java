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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.mixare.data.DataSourceList;
import org.mixare.data.DataSourceStorage;
import org.mixare.gui.HudView;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.Matrix;
import org.mixare.map.MixMap;
import org.mixare.mapimage.ProjectedMap;
import org.mixare.mgr.HttpTools;

import java.util.Date;
import java.util.Random;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;


/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */
public class MixViewActivity extends MixMenu implements SensorEventListener, OnTouchListener {

	private CameraSurface cameraSurface;
    private FrameLayout cameraView;
//	private AugmentedView augmentedView;
    private ProjectedMapView projectedMapView;
    public HudView hudView;

    private boolean isInited;
	private static PaintScreen paintScreen;
	private static MarkerRenderer markerRenderer;
	private boolean fError;

	/* Different error messages */
	protected static final int UNSUPPORTED_HARDWARE = 0;
	protected static final int GPS_ERROR = 1;
	public static final int GENERAL_ERROR = 2;
	protected static final int NO_NETWORK_ERROR = 4;

	private static final int PERMISSIONS_REQUEST_CAMERA = 1;
	private static final int PERMISSIONS_ACCESS_FINE_LOCATION = 2;
	private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 3;



	private MixViewDataHolder mixViewData;

	//private GLSurfaceView mGLSurfaceView;
	private SensorManager mSensorManager;
	//private RotationVektorRenderer mRenderer;
	private TouchSurfaceView cubeView;
	private CubeRenderer mRenderer;
	private Sensor mOrienation;


	/**
	 * Main application Launcher.
	 * Does:
	 * - Lock Screen.
	 * - Initiate Camera View
	 * - Initiate markerRenderer {@link MarkerRenderer#draw() MarkerRenderer}
	 * - Display License Agreement if mixViewActivity first used.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			handleIntent(getIntent());



            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			getMixViewData().setSensorMgr((SensorManager) getSystemService(SENSOR_SERVICE));


			killOnError();
			//requestWindowFeature(Window.FEATURE_NO_TITLE);

			if(getSupportActionBar() != null){
				getSupportActionBar().hide();
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

				if (ContextCompat.checkSelfPermission(this,
						Manifest.permission.CAMERA)
						!= PackageManager.PERMISSION_GRANTED) {

					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.CAMERA},
							PERMISSIONS_REQUEST_CAMERA);
				}
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							PERMISSIONS_ACCESS_FINE_LOCATION);
				}
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
					ActivityCompat.requestPermissions(this,
							new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
							PERMISSIONS_WRITE_EXTERNAL_STORAGE);
				}
			}

			maintainViews();
			maintainRotationVektorDemo();

//			augmentedView.setOnTouchListener(new View.OnTouchListener() {
//				@Override
//				public boolean onTouch(View view, MotionEvent me) {
//                    hudView.hideRangeBar();
//
//                    try {
//						killOnError();
//						float xPress = me.getX();
//						float yPress = me.getY();
//						if (me.getAction() == MotionEvent.ACTION_UP) {
//							getMarkerRenderer().clickEvent(xPress, yPress);
//						}
//						return true;
//					} catch (Exception ex) {
//						// doError(ex);
//						ex.printStackTrace();
//						//return super.onTouchEvent(me);
//					}
//					return true;
//				}
//
//			});


			if (!isInited) {
				setPaintScreen(new PaintScreen());
                getMarkerRenderer();

				refreshDownload();
				isInited = true;
			}

			/*
			 * Get the preference file PREFS_NAME stored in the internal memory
			 * of the phone
			 */
			SharedPreferences settings = getSharedPreferences(Config.PREFS_NAME, 0);

			/* check if the application is launched for the first time */
			if (!settings.getBoolean("firstAccess", false)) {
				firstAccess(settings);
			}
		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {


		switch (requestCode) {
			case PERMISSIONS_REQUEST_CAMERA: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					maintainViews();

				} else {

				}
				return;
			}

		}
	}

	@Override
	public MixViewDataHolder getMixViewData() {
		MixContext.setActualMixViewActivity(this);
		return MixViewDataHolder.getInstance();
	}

	/**
	 * Part of Android LifeCycle that gets called when "Activity" MixViewActivity is
	 * being navigated away. <br/>
	 * Does: - Release Screen Lock - Unregister Sensors.
	 * {@link android.hardware.SensorManager SensorManager} - Unregister
	 * Location Manager. {@link org.mixare.mgr.location.LocationFinder
	 * LocationFinder} - Switch off Download Thread.
	 * {@link org.mixare.mgr.downloader.DownloadManager DownloadManager} -
	 * Cancel markerRenderer refresh Timer. <br/>
	 * {@inheritDoc}
	 */
	@Override
	protected void onPause() {
		super.onPause();
		try {
			cameraSurface.surfaceDestroyed(null);
			mSensorManager.unregisterListener(this);

			try {
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorGrav());
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorMag());
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorGyro());
				getMixViewData().getSensorMgr().unregisterListener(this);
				getMixViewData().setSensorGrav(null);
				getMixViewData().setSensorMag(null);
				getMixViewData().setSensorGyro(null);

				MixContext.getInstance().getLocationFinder()
						.switchOff();
				MixContext.getInstance().getDownloadManager()
						.switchOff();

				MixContext.getInstance().getNotificationManager()
						.setEnabled(false);
				MixContext.getInstance().getNotificationManager()
						.clear();
				if (getMarkerRenderer() != null) {
					getMarkerRenderer().cancelRefreshTimer();
				}
			} catch (Exception ignore) {
			}

			if (fError) {
				finish();
			}
		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
		}
	}

	/**
	 * Mixare Activities Pipe message communication.
	 * Receives results from other launched activities
	 * and base on the result returned, it either refreshes screen or not.
	 * Default value for refreshing is false
	 * <br/>
	 * {@inheritDoc}
	 */
	protected void onActivityResult(final int requestCode,
									final int resultCode, Intent data) {
		//Log.d(TAG + " WorkFlow", "MixViewActivity - onActivityResult Called");
		// check if the returned is request to refresh screen (setting might be
		// changed)
		
		if (requestCode == 35) {
			if (resultCode == 1) {
				final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

				dialog.setTitle(R.string.launch_plugins);
				dialog.setMessage(R.string.plugins_changed);
				dialog.setCancelable(false);
				
				// Always activate new plugins
				
//				final CheckBox checkBox = new CheckBox(ctx);
//				checkBox.setText(R.string.remember_this_decision);
//				dialog.setView(checkBox);		
				
				dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int whichButton) {
						startActivity(new Intent(MixContext.getInstance().getApplicationContext(),
								PluginLoaderActivity.class));
						finish();
					}
				});

				dialog.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int whichButton) {
						d.dismiss();
					}
				});

				dialog.show();
			}
		}
		try {
			if (data.getBooleanExtra("RefreshScreen", false)) {
				Log.d(Config.TAG + " WorkFlow",
						"MixViewActivity - Received Refresh Screen Request .. about to refresh");
				repaint();
				refreshDownload();
			}
		} catch (Exception ex) {
			// do nothing do to mix of return results.
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * Part of Android LifeCycle that gets called when "MixViewActivity" resumes.
	 * <br/>
	 * Does:
	 * - Acquire Screen Lock
	 * - Refreshes Data and Downloads
	 * - Initiate four Matrixes that holds user's rotation markerRenderer.
	 * - Re-register Sensors. {@link android.hardware.SensorManager SensorManager}
	 * - Re-register Location Manager. {@link org.mixare.mgr.location.LocationFinder LocationFinder}
	 * - Switch on Download Thread. {@link org.mixare.mgr.downloader.DownloadManager DownloadManager}
	 * - restart markerRenderer refresh Timer.
	 * <br/>
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(cubeView != null) {
			//mRenderer.start();
			cubeView.onResume();
		}
		mSensorManager.registerListener(cubeView, mOrienation, SensorManager.SENSOR_DELAY_NORMAL);

		try {
			killOnError();
			MixContext.setActualMixViewActivity(this);
			HttpTools.setContext(MixContext.getInstance());
			
			//repaint(); //repaint when requested
			getMarkerRenderer().doStart();
			getMarkerRenderer().clearEvents();
			MixContext.getInstance().getNotificationManager().setEnabled(true);
			refreshDownload();
			
			MixContext.getInstance().getDataSourceManager().refreshDataSources();

			float angleX, angleY;

			int marker_orientation = -90;

			int rotation = Compatibility.getRotation(this);

			// display text from left to right and keep it horizontal
			angleX = (float) Math.toRadians(marker_orientation);
			getMixViewData().getM1().set(1f, 0f, 0f, 0f,
					(float) Math.cos(angleX),
					(float) -Math.sin(angleX), 0f,
					(float) Math.sin(angleX),
					(float) Math.cos(angleX));
			angleX = (float) Math.toRadians(marker_orientation);
			angleY = (float) Math.toRadians(marker_orientation);
			if (rotation == 1) {
				getMixViewData().getM2().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleX),
						(float) -Math.sin(angleX), 0f,
						(float) Math.sin(angleX),
						(float) Math.cos(angleX));
				getMixViewData().getM3().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} else {
				getMixViewData().getM2().set((float) Math.cos(angleX), 0f,
						(float) Math.sin(angleX), 0f, 1f, 0f,
						(float) -Math.sin(angleX), 0f,
						(float) Math.cos(angleX));
				getMixViewData().getM3().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleY),
						(float) -Math.sin(angleY), 0f,
						(float) Math.sin(angleY),
						(float) Math.cos(angleY));

			}

			getMixViewData().getM4().toIdentity();

			for (int i = 0; i < getMixViewData().getHistR().length; i++) {
				getMixViewData().getHistR()[i] = new Matrix();
			}

			getMixViewData().addListSensors(getMixViewData().getSensorMgr().getSensorList(
					Sensor.TYPE_ACCELEROMETER));
			if (getMixViewData().getSensor(0).getType() == Sensor.TYPE_ACCELEROMETER ) {
				getMixViewData().setSensorGrav(getMixViewData().getSensor(0));
			}//else report error (unsupported hardware)

			getMixViewData().addListSensors(getMixViewData().getSensorMgr().getSensorList(
					Sensor.TYPE_MAGNETIC_FIELD));
			if (getMixViewData().getSensor(1).getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				getMixViewData().setSensorMag(getMixViewData().getSensor(1));
			}//else report error (unsupported hardware)
			
			if (!getMixViewData().getSensorMgr().getSensorList(Sensor.TYPE_GYROSCOPE).isEmpty()){
				getMixViewData().addListSensors(getMixViewData().getSensorMgr().getSensorList(
						Sensor.TYPE_GYROSCOPE));
				if (getMixViewData().getSensor(2).getType() == Sensor.TYPE_GYROSCOPE) {
					getMixViewData().setSensorGyro(getMixViewData().getSensor(2));
				}
				getMixViewData().getSensorMgr().registerListener(this,
						getMixViewData().getSensorGyro(), SENSOR_DELAY_GAME);
			}
			
				getMixViewData().getSensorMgr().registerListener(this,
						getMixViewData().getSensorGrav(), SENSOR_DELAY_GAME);
				getMixViewData().getSensorMgr().registerListener(this,
						getMixViewData().getSensorMag(), SENSOR_DELAY_GAME);
				
			try {
				GeomagneticField gmf = MixContext.getInstance()
						.getLocationFinder().getGeomagneticField();
				angleY = (float) Math.toRadians(-gmf.getDeclination());
				getMixViewData().getM4().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} catch (Exception ex) {
				doError(ex, GPS_ERROR);
			}

			if (!isNetworkAvailable()) {
				Log.d(Config.TAG, "no network");
				doError(null, NO_NETWORK_ERROR);
			} else {
				Log.d(Config.TAG, "network");
			}
			
			MixContext.getInstance().getDownloadManager().switchOn();
			MixContext.getInstance().getLocationFinder().switchOn();
		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
			try {
				if (getMixViewData().getSensorMgr() != null) {
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorGrav());
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorMag());
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorGyro());
					getMixViewData().setSensorMgr(null);
				}

				if (MixContext.getInstance() != null) {
					MixContext.getInstance().getLocationFinder()
							.switchOff();
					MixContext.getInstance().getDownloadManager()
							.switchOff();
				}
			} catch (Exception ignore) {
			}
		} finally {
			//This does not conflict with registered sensors (sensorMag, sensorGrav)
			//This is a place holder to API returned listed of sensors, we registered
			//what we need, the rest is unnecessary.
			getMixViewData().clearAllSensors();
		}

		Log.d(Config.TAG, "resume");
		if (getMarkerRenderer() == null) {
			return;
		}
		if (getMarkerRenderer().isFrozen()
				&& getMixViewData().getSearchNotificationTxt() == null) {
			getMixViewData().setSearchNotificationTxt(new TextView(this));
			getMixViewData().getSearchNotificationTxt().setWidth(
					getPaintScreen().getWidth());
			getMixViewData().getSearchNotificationTxt().setPadding(10, 2, 0, 0);
			getMixViewData().getSearchNotificationTxt().setText(
					getString(R.string.search_active_1) + " "
							+ DataSourceList.getDataSourcesStringList()
							+ getString(R.string.search_active_2));
			;
			getMixViewData().getSearchNotificationTxt().setBackgroundColor(
					Color.DKGRAY);
			getMixViewData().getSearchNotificationTxt().setTextColor(
					Color.WHITE);

			getMixViewData().getSearchNotificationTxt()
					.setOnTouchListener(this);
			addContentView(getMixViewData().getSearchNotificationTxt(),
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		} else if (!getMarkerRenderer().isFrozen()
				&& getMixViewData().getSearchNotificationTxt() != null) {
			getMixViewData().getSearchNotificationTxt()
					.setVisibility(View.GONE);
			getMixViewData().setSearchNotificationTxt(null);
		}
	}

	/**
	 * Customize Activity after switching back to it.
	 * Currently it maintain and ensures markerRenderer creation.
	 * <br/>
	 * {@inheritDoc}
	 */
	protected void onRestart() {
		super.onRestart();
		maintainViews();
	}

	/**
	 * {@inheritDoc}
	 * Deallocate memory and stops threads.
	 * Please don't rely on this function as it's killable,
	 * and might not be called at all.
	 */
	protected void onDestroy(){
		try{
			
			MixContext.getInstance().getDownloadManager().shutDown();
			getMixViewData().getSensorMgr().unregisterListener(this);
			getMixViewData().setSensorMgr(null);
			/*
			 * Invoked when the garbage collector has detected that this
			 * instance is no longer reachable. The default implementation does
			 * nothing, but this method can be overridden to free resources.
			 * 
			 * Do we have to create our own finalize?
			 */
		} catch (Exception e) {
			//do nothing we are shutting down
		} catch (Throwable e) {
			//finalize error. (this function does nothing but call native API and release 
			//any synchronization-locked messages and threads deadlocks.
			Log.e(Config.TAG, e.getMessage());
		} finally {
			super.onDestroy();
		}
	}

	private void maintainViews() {
		maintainCamera();
		maintainAugmentedView();
		if (Config.useHUD) {
			maintainHudView();
		}
		maintainProjectedMapView();
	}

	private void maintainProjectedMapView() {
        if (projectedMapView == null) {
            projectedMapView = new ProjectedMapView(this);
            cameraView.addView(projectedMapView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
        } else{
            ((ViewGroup) projectedMapView.getParent()).removeView(projectedMapView);
            cameraView.addView(projectedMapView, new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
        }
//		projectedMapImage.setCurrentLocation(MixContext.getInstance().getLocationFinder().getCurrentLocation());
//        Bitmap projectMap = projectedMapImage.getProjectedMap();
//        if (projectMap == null) return;
//        Canvas drawMap = new Canvas(projectMap);
//        drawMap.drawBitmap(projectMap, 0, 0, null);
//		mapImage.draw(drawMap);
//		mapImage.setImageBitmap(projectedMapImage.getProjectedMap());
//		mapImage.setAlpha(0.4f);
	}
	
	/* ********* Operators ***********/

	/**
	 * View Repainting.
	 * It deletes viewed data and initiate new one. {@link MarkerRenderer MarkerRenderer}
	 */
	public void repaint() {
		// clear stored data
		getMarkerRenderer().clearEvents();
		setPaintScreen(new PaintScreen());
    }

	/**
	 * Checks cameraSurface, if it does not exist, it creates one.
	 */
	private void maintainCamera() {

		cameraView = (FrameLayout) findViewById(R.id.content_frame);



			if (cameraSurface == null) {
				cameraSurface = new CameraSurface(this);
				cameraView.addView(cameraSurface);
			} else {
				cameraView.removeView(cameraSurface);
				cameraView.addView(cameraSurface);

			}
			//setContentView(cameraSurface);
		}

	/**
	 * Checks augmentedView, if it does not exist, it creates one.
	 */
	private void maintainAugmentedView() {
//		if (augmentedView == null) {
//			augmentedView = new AugmentedView(this);
//			cameraView.addView(augmentedView, new LayoutParams(LayoutParams.WRAP_CONTENT,
//					LayoutParams.WRAP_CONTENT));
//			//addContentView(augScreen, new LayoutParams(LayoutParams.WRAP_CONTENT,
//			//		LayoutParams.WRAP_CONTENT));
//		}
//		else{
//
//			((ViewGroup) augmentedView.getParent()).removeView(augmentedView);
//			//addContentView(augmentedView, new LayoutParams(LayoutParams.WRAP_CONTENT,
//			//		LayoutParams.WRAP_CONTENT));
//			cameraView.addView(augmentedView, new LayoutParams(LayoutParams.WRAP_CONTENT,
//					LayoutParams.WRAP_CONTENT));
//		}

	}

	/**
	 * Checks HUD GUI, if it does not exist, it creates one.
	 */
	private void maintainHudView() {
		if (hudView == null) {
            hudView = new HudView(this);
		}
        else {
            ((ViewGroup) hudView.getParent()).removeView(hudView);
        }
        addContentView(hudView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    }


	private void maintainRotationVektorDemo() {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mOrienation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);


		mRenderer = new CubeRenderer ();
		cubeView = new TouchSurfaceView(this, mSensorManager);

/*
		mGLSurfaceView.requestFocus();
		mGLSurfaceView.setFocusableInTouchMode(true);
		mGLSurfaceView.setZOrderOnTop(true);
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		mGLSurfaceView.setRenderer(mRenderer);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		*/

	}
	/**
	 * Refreshes Download TODO refresh downloads
	 */
	public void refreshDownload(){
		MixContext.getInstance().getDownloadManager().switchOn();
//		try {
//			if (getMixViewData().getDownloadThread() != null){
//				if (!getMixViewData().getDownloadThread().isInterrupted()){
//					getMixViewData().getDownloadThread().interrupt();
//					MixContext.getInstance().getDownloadManager().restart();
//				}
//			}else { //if no download thread found
//				getMixViewData().setDownloadThread(new Thread(getMixViewData()
//						.getMixContext().getDownloadManager()));
//				//@TODO Syncronize DownloadManager, call Start instead of run.
//				mixViewData.getMixContext().getDownloadManager().run();
//			}
//		}catch (Exception ex){
//		}
	}
	
	/**
	 * Refreshes Viewed Data.
	 */
	public void refresh(){
		markerRenderer.refresh();
	}

	public void setErrorDialog(int error) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		switch (error) {
		case NO_NETWORK_ERROR:
			builder.setMessage(getString(R.string.connection_error_dialog));
			break;
		case GPS_ERROR:
			builder.setMessage(getString(R.string.gps_error_dialog));
			break;
		case GENERAL_ERROR:
			builder.setMessage(getString(R.string.general_error_dialog));
			break;
		case UNSUPPORTED_HARDWARE:
			builder.setMessage(getString(R.string.unsupportet_hardware_dialog));
			break;
		}

		/*Retry*/
		builder.setPositiveButton(R.string.connection_error_dialog_button1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // "restart" mixare
                startActivity(new Intent(MixContext.getInstance().getApplicationContext(),
                        PluginLoaderActivity.class));
                finish();
            }
        });
		if (error == GPS_ERROR) {
			/* Open settings */
			builder.setNeutralButton(R.string.connection_error_dialog_button2,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								Intent intent1 = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(intent1, 42);
							} catch (Exception e) {
								Log.d(Config.TAG, "No Location Settings");
							}
						}
					});
		} else if (error == NO_NETWORK_ERROR) {
			builder.setNeutralButton(R.string.connection_error_dialog_button2,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								Intent intent1 = new Intent(
										Settings.ACTION_DATA_ROAMING_SETTINGS);
								ComponentName cName = new ComponentName(
										"com.android.phone",
										"com.android.phone.Settings");
								intent1.setComponent(cName);
								startActivityForResult(intent1, 42);
							} catch (Exception e) {
								Log.d(Config.TAG, "No Network Settings");
							}
						}
					});
		}
		/*Close application*/
		builder.setNegativeButton(R.string.connection_error_dialog_button3, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
		
		AlertDialog alert = builder.create();
		alert.show();
	}

    /**
	 * Handle First time users. It display license agreement and store user's
	 * acceptance.
	 * 
	 * @param settings
	 */
	private void firstAccess(SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
		builder1.setMessage(getString(R.string.license));
		builder1.setNegativeButton(getString(R.string.close_button),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
		AlertDialog alert1 = builder1.create();
		alert1.setTitle(getString(R.string.license_title));
		alert1.show();
		editor.putBoolean("firstAccess", true);

		// value for maximum POI for each selected OSM URL to be active by
		// default is 5
		editor.putInt("osmMaxObject", 5);
		editor.commit();

		// add the default datasources to the preferences file
		DataSourceStorage.getInstance().fillDefaultDataSources();
	}



	/**
	 * Checks whether a network is available or not
	 * @return True if connected, false if not
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void selectItem(int position) {
		int menuItemId=getResources().obtainTypedArray(R.array.menu_item_titles).getResourceId(position,-1);
		switch (menuItemId) {
		/* Data sources */
			case R.string.menu_item_datasources:
				if (!getMarkerRenderer().getIsLauncherStarted()) {
					Intent intent = new Intent(MixViewActivity.this, DataSourceList.class);
					startActivityForResult(intent, Config.INTENT_REQUEST_CODE_DATASOURCES);
				} else {
					markerRenderer.getContext().getNotificationManager()
							.addNotification(getString(R.string.no_website_available));
				}
				break;
			/* Plugin View */
			case R.string.menu_item_plugins:
				if (!getMarkerRenderer().getIsLauncherStarted()) {
					Intent intent = new Intent(MixViewActivity.this,
							PluginListActivity.class);
					startActivityForResult(intent, Config.INTENT_REQUEST_CODE_PLUGINS);
				} else {
					markerRenderer.getContext().getNotificationManager()
							.addNotification(getString(R.string.no_website_available));
				}
				break;
		/* List markerRenderer */
			case R.string.menu_item_list:
			/*
			 * if the list of titles to show in alternative list markerRenderer is not
			 * empty
			 */
				if (getMarkerRenderer().getDataHandler().getMarkerCount() > 0) {
					Intent intent1 = new Intent(MixViewActivity.this, MarkerListActivity.class);
					intent1.setAction(Intent.ACTION_VIEW);
					startActivityForResult(intent1, Config.INTENT_REQUEST_CODE_MARKERLIST);
				}
			/* if the list is empty */
				else {
					markerRenderer.getContext().getNotificationManager().
							addNotification(getString(R.string.empty_list));
				}
				break;
		/* Map View */
			case R.string.menu_item_map:
				Intent intent2 = new Intent(MixViewActivity.this, MixMap.class);
				startActivityForResult(intent2, Config.INTENT_REQUEST_CODE_MAP);
				break;
		/* range level */
			case R.string.menu_item_range:
                hudView.showRangeBar();
                drawerLayout.closeDrawer(drawerList);
                break;
		/* Search */
			case R.string.menu_item_search:
				onSearchRequested();
				break;
		/* GPS Information */
			case R.string.menu_item_info:
				Location currentGPSInfo = MixContext.getInstance()
						.getLocationFinder().getCurrentLocation();
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(getString(R.string.general_info_text) + "\n\n"
						+ getString(R.string.longitude)
						+ currentGPSInfo.getLongitude() + "\n"
						+ getString(R.string.latitude)
						+ currentGPSInfo.getLatitude() + "\n"
						+ getString(R.string.altitude)
						+ currentGPSInfo.getAltitude() + "m\n"
						+ getString(R.string.speed) + currentGPSInfo.getSpeed()
						+ "km/h\n" + getString(R.string.accuracy)
						+ currentGPSInfo.getAccuracy() + "m\n"
						+ getString(R.string.gps_last_fix)
						+ new Date(currentGPSInfo.getTime()).toString() + "\n");
				builder.setNegativeButton(getString(R.string.close_button),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
				AlertDialog alert = builder.create();
				alert.setTitle(getString(R.string.general_info_title));
				alert.show();
				break;
		/* license agreement */
			case R.string.menu_item_license:
				AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
				builder1.setMessage(getString(R.string.license));
			/* Retry */
				builder1.setNegativeButton(getString(R.string.close_button),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						});
				AlertDialog alert1 = builder1.create();
				alert1.setTitle(getString(R.string.license_title));
				alert1.show();
				break;
			case R.string.menu_item_test_augmentedview:

				Location curLocation = MixViewDataHolder.getInstance().getCurLocation();
				Log.d(Config.TAG, "info 1: aktuelle Postition: " + curLocation.getLongitude() + ", " + curLocation.getLatitude());

				if(!cubeView.isAttachedToWindow()) {
					cameraView.addView(cubeView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				}
				else {
					cameraView.removeView(cubeView);
				}
				break;
			/* test destination selection (from marker) */
			case R.string.menu_item_route:
                new MarkerListFragment().show(getFragmentManager(), "TAG");
				break;
            /* some random error (for testing?!)*/
			default:
				doError(null, new Random().nextInt(3));
				break;
		}

	}

	public void switchToMixMap(){
		Intent mixMapIntent = new Intent(this, MixMap.class);
		startActivity(mixMapIntent);
	}

	public void onSensorChanged(SensorEvent evt) {
		try {
			if (getMixViewData().getSensorGyro() != null) {
				
				if (evt.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
					getMixViewData().setGyro(evt.values);
				}
				
				if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					getMixViewData().setGrav(
							getMixViewData().getGravFilter().lowPassFilter(evt.values,
									getMixViewData().getGrav()));
				} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					getMixViewData().setMag(
							getMixViewData().getMagFilter().lowPassFilter(evt.values,
									getMixViewData().getMag()));
				}
				getMixViewData().setAngle(
						getMixViewData().getMagFilter().complementaryFilter(
								getMixViewData().getGrav(),
								getMixViewData().getGyro(), 30,
								getMixViewData().getAngle()));
				
				SensorManager.getRotationMatrix(
						getMixViewData().getRTmp(),
						getMixViewData().getI(), 
						getMixViewData().getGrav(),
						getMixViewData().getMag());
			} else {
				if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					getMixViewData().setGrav(evt.values);
				} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					getMixViewData().setMag(evt.values);
				}
				SensorManager.getRotationMatrix(
						getMixViewData().getRTmp(),
						getMixViewData().getI(), 
						getMixViewData().getGrav(),
						getMixViewData().getMag());
			}
			
//			augmentedView.postInvalidate();
			hudView.postInvalidate();
            projectedMapView.postInvalidate();

            int rotation = Compatibility.getRotation(this);

			if (rotation == 1) {
				SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
						SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
						getMixViewData().getRot());
			} else {
				SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
						SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
						getMixViewData().getRot());
			}
			getMixViewData().getTempR().set(getMixViewData().getRot()[0],
					getMixViewData().getRot()[1], getMixViewData().getRot()[2],
					getMixViewData().getRot()[3], getMixViewData().getRot()[4],
					getMixViewData().getRot()[5], getMixViewData().getRot()[6],
					getMixViewData().getRot()[7], getMixViewData().getRot()[8]);

			getMixViewData().getFinalR().toIdentity();
			getMixViewData().getFinalR().prod(getMixViewData().getM4());
			getMixViewData().getFinalR().prod(getMixViewData().getM1());
			getMixViewData().getFinalR().prod(getMixViewData().getTempR());
			getMixViewData().getFinalR().prod(getMixViewData().getM3());
			getMixViewData().getFinalR().prod(getMixViewData().getM2());
			getMixViewData().getFinalR().invert();
			
			getMixViewData().getHistR()[getMixViewData().getrHistIdx()]
					.set(getMixViewData().getFinalR());
			
			int histRLenght = getMixViewData().getHistR().length;
			
			getMixViewData().setrHistIdx(getMixViewData().getrHistIdx() + 1);
			if (getMixViewData().getrHistIdx() >= histRLenght)
				getMixViewData().setrHistIdx(0);

			getMixViewData().getSmoothR().set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
                    0f);
			for (int i = 0; i < histRLenght; i++) {
				getMixViewData().getSmoothR().add(
						getMixViewData().getHistR()[i]);
			}
			getMixViewData().getSmoothR().mult(
                    1 / (float) histRLenght);

			MixContext.getInstance().updateSmoothRotation(
                    getMixViewData().getSmoothR());

//			float[] orientation = new float[3];
//            float[] rTemp = getMixViewData().getRTmp();
//			SensorManager.getOrientation(rTemp, orientation);
//            Log.d("PROJECT_MAP", "Sensor Angles");
//			Log.d("PROJECT_MAP", orientation[0] + ", " + orientation[1] + ", " + orientation[2]);
//            Log.d("PROJECT_MAP", "Rotation Matrix Before Smoothing");
//            Log.d("PROJECT_MAP", rTemp[0] + ", " + rTemp[1] + ", " + rTemp[2]);
//            Log.d("PROJECT_MAP", rTemp[3] + ", " + rTemp[4] + ", " + rTemp[5]);
//            Log.d("PROJECT_MAP", rTemp[6] + ", " + rTemp[7] + ", " + rTemp[8]);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		hudView.hideRangeBar();
		
		try {
			killOnError();

			float xPress = me.getX();
			float yPress = me.getY();
			if (me.getAction() == MotionEvent.ACTION_UP) {
				getMarkerRenderer().clickEvent(xPress, yPress);
			}// TODO add gesture events (low)

			return true;
		} catch (Exception ex) {
			// doError(ex);
			ex.printStackTrace();
			return super.onTouchEvent(me);
		}
	}

    /*
     * Handler for physical key presses (menu key, back key)
     */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			killOnError();

			//if range bar was visible, hide it
			if (hudView.isRangeBarVisible()) {
                hudView.hideRangeBar();
				if (keyCode == KeyEvent.KEYCODE_MENU) {
					return super.onKeyDown(keyCode, event);
				}
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (getMarkerRenderer().isDetailsView()) {
					getMarkerRenderer().keyEvent(keyCode);
					getMarkerRenderer().setDetailsView(false);
					return true;
				} else {
					Intent close = new Intent();
					close.putExtra("closed", "MixViewActivity");
					setResult(0, close);
					finish();
					return super.onKeyDown(keyCode, event);
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return super.onKeyDown(keyCode, event);
			} else {
				getMarkerRenderer().keyEvent(keyCode);
				return false;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return super.onKeyDown(keyCode, event);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
				&& accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
				&& getMixViewData().getCompassErrorDisplayed() == 0) {
			for (int i = 0; i < 2; i++) {
				markerRenderer.getContext().getNotificationManager().
				addNotification("Compass data unreliable. Please recalibrate compass.");
			}
			getMixViewData().setCompassErrorDisplayed(
					getMixViewData().getCompassErrorDisplayed() + 1);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		getMarkerRenderer().setFrozen(false);
		if (getMixViewData().getSearchNotificationTxt() != null) {
			getMixViewData().getSearchNotificationTxt()
					.setVisibility(View.GONE);
			getMixViewData().setSearchNotificationTxt(null);
		}
		return true;
	}

	/* ************ Handlers ************ */

	public void doError(Exception ex1, int error) {
		if (!fError) {
			fError = true;

			setErrorDialog(error);

			try {
				ex1.printStackTrace();
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

		try {
//			augmentedView.invalidate();
		} catch (Exception ignore) {
		}
	}

	public void killOnError() throws Exception {
		if (fError)
			throw new Exception();
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			intent.setClass(this, MarkerListActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}


	/* ******* Getter and Setters ********** */

	/**
	 * @return the paintScreen
	 */
	static PaintScreen getPaintScreen() {
		return paintScreen;
	}

	/**
	 * @param paintScreen the paintScreen to set
	 */
	static void setPaintScreen(PaintScreen paintScreen) {
		MixViewActivity.paintScreen = paintScreen;
	}

	/**
	 * @return the markerRenderer
	 */
	public MarkerRenderer getMarkerRenderer() {
        if(markerRenderer==null){
            markerRenderer=new MarkerRenderer(MixContext.getInstance());
        }
		return markerRenderer;
	}

    /**
     * @return the markerRenderer statically - only to be used in other activities/views
     */
    public static MarkerRenderer getMarkerRendererStatically() {
        if(markerRenderer==null){
            Log.d(Config.TAG,"markerRenderer was null (called statically)");
        }
        return markerRenderer;
    }

    public void updateHud(Location curFix){
        if(Config.useHUD) {
            hudView.updatePositionStatus(curFix);
            hudView.setDataSourcesStatus(getMarkerRenderer().dataSourceWorking, false, null);
            hudView.setDestinationStatus(getMixViewData().getCurDestination());
        }
    }
}

