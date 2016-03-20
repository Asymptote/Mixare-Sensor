/*
 * Copyleft 2012 - Peer internet solutions 
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
package org.mixare.mgr.datasource;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mixare.MixContext;
import org.mixare.data.DataSource;
import org.mixare.data.DataSourceStorage;
import org.mixare.mgr.downloader.DownloadRequest;
import org.mixare.mgr.location.LocationBlur;

class DataSourceMgrImpl implements DataSourceManager {

	private final ConcurrentLinkedQueue<DataSource> allDataSources = new ConcurrentLinkedQueue<>();

	private final MixContext ctx;

	public DataSourceMgrImpl(MixContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isAtLeastOneDataSourceSelected() {
		for (DataSource ds : this.allDataSources) {
			if (ds.getEnabled()) {
				return true;
			}
		}
		return false;
	}

	public void setAllDataSourcesForLauncher(DataSource datasource) {
		this.allDataSources.clear(); // TODO WHY? CLEAN ALL
		this.allDataSources.add(datasource);
	}

	public void refreshDataSources() {
		this.allDataSources.clear();

		int size;
		size = DataSourceStorage.getInstance(ctx).getSize();

		// copy the value from shared preference to adapter
		for (int i = 0; i < size; i++) {
			this.allDataSources.add(DataSourceStorage.getInstance()
					.getDataSource(i));
		}
	}

	public void requestDataFromAllActiveDataSource(double lat, double lon,
			double alt, float radius) {
		for (DataSource ds : allDataSources) {
			if (ds.getEnabled()) {
				requestData(ds, lat, lon, alt, radius, Locale.getDefault()
						.getLanguage());
			}
		}

	}

	private void requestData(DataSource datasource, double lat, double lon,
			double alt, float radius, String locale) {
		double[] result = { lat, lon };
		if (datasource.getBlur().equals(DataSource.BLUR.TRUNCATE)) {
			result = LocationBlur.truncateLocation(result[0], result[1]);
		} else if (datasource.getBlur().equals(DataSource.BLUR.ADD_RANDOM)) {
			result = LocationBlur.addRandomDistance(result[0], result[1]);
		}
		
		DownloadRequest request = new DownloadRequest(datasource,
				datasource.createRequestParams(result[0], result[1], alt, radius, locale));
		ctx.getDownloadManager().submitJob(request);

	}
}