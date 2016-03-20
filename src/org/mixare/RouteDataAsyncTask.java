package org.mixare;

import android.location.Location;
import android.os.AsyncTask;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelanieW on 03.02.2016.
 */
public class RouteDataAsyncTask extends AsyncTask<Location,Void,List<LatLong>> {

    List<LatLong> coordinateList = new ArrayList<>();
    List<LatLong> latLong = new ArrayList<>();

        public AsyncResponse delegate = null;

        public RouteDataAsyncTask(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected List<LatLong> doInBackground(Location... params) {
            RouteData rs = new RouteData();
            latLong = rs.init(params[0],params[1]);
            return latLong;
        }

        @Override
        protected void onPostExecute(List<LatLong> latLongs) {
            super.onPostExecute(latLongs);
            for (LatLong latlong : latLongs) {
                coordinateList.add(latlong);
            }
            delegate.processFinish(coordinateList);
        }
}
