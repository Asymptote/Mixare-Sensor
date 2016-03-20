package org.mixare.mapimage;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;

/**
 * Created by Ammar on 3/6/2016.
 */
public class MapLoader extends AsyncTask<Location, Void, Bitmap> {

    public static String TAG = "MAP_LOADER";
    private int width;
    private int height;
    private int zoom;
    private ProjectedMap content;

    public MapLoader(int w, int h, int z, ProjectedMap pM) {
        this.width = w;
        this.height = h;
        this.zoom = z;
        this.content = pM;
    }

    public Bitmap loadMapImage(Location currentLocation) {
        return null;
//        if (currentLocation == null) return null;
//        try {
//            URL url = new URL("https://maps.googleapis.com/maps/api/staticmap?"
//                    + String.format("center=%s,%s&size=%dx%d&zoom=%d", currentLocation.getLatitude(),
//                    currentLocation.getLongitude(), width, height, zoom));
//            Log.d(TAG, "URL to send: " + url.toExternalForm());
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            return BitmapFactory.decodeStream(input);
//        } catch (IOException e) {
//            // Log exception
//            Log.d(TAG, "Exception occurred while loading");
//            return null;
//        }
    }

    @Override
    protected Bitmap doInBackground(Location... params) {
        Bitmap map = loadMapImage(params[0]);
        return map;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        this.content.setMap(bitmap);
    }
}
