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

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.datasource.DataSourceManager;
import org.mixare.mgr.datasource.DataSourceManagerFactory;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadManagerFactory;
import org.mixare.mgr.location.LocationFinder;
import org.mixare.mgr.location.LocationFinderFactory;
import org.mixare.mgr.notification.NotificationManager;
import org.mixare.mgr.notification.NotificationManagerFactory;
import org.mixare.mgr.webcontent.WebContentManager;
import org.mixare.mgr.webcontent.WebContentManagerFactory;

import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;

/**
 * Cares about location management and about the data (source, inputstream)
 */
public class MixContext extends ContextWrapper implements MixContextInterface {
	private static MixViewActivity mixViewActivity;
    private static MixContext instance;

    private final Matrix rotationM = new Matrix();

	/** Responsible for all download */
	private DownloadManager downloadManager;

	/** Responsible for all location tasks */
	private LocationFinder locationFinder;

	/** Responsible for data Source Management */
	private DataSourceManager dataSourceManager;

	/** Responsible for Web Content */
	private WebContentManager webContentManager;
	
	/** Responsible for Notification logging */
	private NotificationManager notificationManager;

    public synchronized static MixContext getInstance()
    {
        if (instance == null)	{
            instance = new MixContext();
        }
        return instance;
    }

	private MixContext() {
		super(mixViewActivity);

		// TODO: RE-ORDER THIS SEQUENCE... IS NECESSARY?
		getDataSourceManager().refreshDataSources();

		if (!getDataSourceManager().isAtLeastOneDataSourceSelected()) {
			rotationM.toIdentity();
		}
		getLocationFinder().switchOn();
		getLocationFinder().findLocation();
	}

	public String getStartUrl() {
		Intent intent = (getActualMixViewActivity()).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString();
		} else {
			return "";
		}
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	@Deprecated
	public void loadMixViewWebPage(String url) throws Exception {

	}

	public void updatePositionStatus(boolean working, boolean problem, String statusText) {

	}

	public void updateDataSourceStatus(boolean working, boolean problem, String statusText) {
		if(Config.useHUD) {
			getActualMixViewActivity().hudView.setDataSourcesStatus(working, problem, statusText);
		}
	}

	public void updateSensorsStatus(boolean working, boolean problem, String statusText) {

	}

	public void updateSmoothRotation(Matrix smoothR) {
		synchronized (rotationM) {
			rotationM.set(smoothR);
		}
	}

	public DataSourceManager getDataSourceManager() {
		if (this.dataSourceManager == null) {
			dataSourceManager = DataSourceManagerFactory
					.makeDataSourceManager(this);
		}
		return dataSourceManager;
	}

	public LocationFinder getLocationFinder() {
		if (this.locationFinder == null) {
			locationFinder = LocationFinderFactory.makeLocationFinder(this);
		}
		return locationFinder;
	}

	public DownloadManager getDownloadManager() {
		if (this.downloadManager == null) {
			downloadManager = DownloadManagerFactory.makeDownloadManager(this);
			getLocationFinder().setDownloadManager(downloadManager);
		}
		return downloadManager;
	}

	public WebContentManager getWebContentManager() {
		if (this.webContentManager == null) {
			webContentManager = WebContentManagerFactory
					.makeWebContentManager(this);
		}
		return webContentManager;
	}

	public NotificationManager getNotificationManager() {
		if (this.notificationManager == null) {
			notificationManager = NotificationManagerFactory
					.makeNotificationManager(this);
		}
		return notificationManager;
	}
	
	public MixViewActivity getActualMixViewActivity() {
		synchronized (mixViewActivity) {
			return mixViewActivity;
		}
	}

	public static void setActualMixViewActivity(MixViewActivity mixViewActivity) {
		synchronized (mixViewActivity) {
			MixContext.mixViewActivity = mixViewActivity;
		}
	}

	public ContentResolver getContentResolver() {
		ContentResolver out = super.getContentResolver();
		if (super.getContentResolver() == null) {
			out = getActualMixViewActivity().getContentResolver();
		}
		return out;
	}
	
	/**
	 * Toast POPUP notification
	 * 
	 * @param string message
	 */
	public void doPopUp(final String string){
		getNotificationManager().addNotification(string);
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param RidOfString RidOfString
	 */
	public void doPopUp(int RidOfString) {
        doPopUp(this.getString(RidOfString));
	}
}
