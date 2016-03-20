package org.mixare.mapimage;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import org.mixare.MixState;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.render.Matrix;

/**
 * Created by Ahsan on 20/3/2016.
 */
public class ProjectedMap {
    public static int WIDTH = 600;
    public static int HEIGHT = 600;
    public static int ZOOM = 18;
    public static float THRESHOLD = 0.1f;
    public static String TAG = "PROJECT_MAP";
    public static double ALTITUDE = 1.6;

    //start -- Ahsan's code
    protected MixVector locationVector = new MixVector(-600.0f, -600.0f, 500.0f);
    public MixVector cMarker = new MixVector();
    private final MixState state = new MixState();
    //end -- Ahsan's code

    Location currentLocation;
    android.graphics.Matrix finalMatrix;
    Bitmap map;
    Bitmap project;

    public ProjectedMap() {
    }

    public void setCurrentLocation(Location loc) {
        if (loc == null)
            return;

        if (currentLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                    this.currentLocation.getLatitude(), this.currentLocation.getLongitude(), results);
            if (results[0] > THRESHOLD)
                new MapLoader(WIDTH, HEIGHT, ZOOM, this).execute(loc);
            else
                return;
        } else {
            new MapLoader(WIDTH, HEIGHT, ZOOM, this).execute(loc);
        }

        Log.d(TAG, "Setting Current Location");
        this.currentLocation = loc;
    }

    public void setMap(Bitmap map) {
        this.map = map;
    }

    public Bitmap getMap() {
        return map;
    }

    public Bitmap getProjectedMap() {
        return project;
    }

    public android.graphics.Matrix getMatrix() {
        return finalMatrix;
    }

    public MixVector getcMarker() {
        return cMarker;
    }

    public void projectMap(Camera viewCam) {
//        if (map == null || viewCam == null) return;
        if (viewCam == null) return;


        if (project != null)
            project.recycle();

        state.calcPitchBearing(viewCam.transform);

        //*******************************
        Matrix transformMatrix = new Matrix();
        transformMatrix.toIdentity();
        transformMatrix.a3 += locationVector.x;
        transformMatrix.b3 += locationVector.y;
        transformMatrix.c3 += locationVector.z;

        Matrix cameraMatrix = new Matrix();
        float fx = 786.429f;
        float cx = 311.253f;
        float cy = 217.013f;
        cameraMatrix.set(fx, 0, cx, 0, fx, cy, 0, 0, 1);

        //Simply apply the inverted rotation matrix
        //Matrix rot = viewCam.transform;
        //rot.invert();

        Matrix xRot = new Matrix();
        float angle = (float) state.getxAng() + (float)Math.PI/2.f;
        xRot.toXRot(angle);
        //xRot.invert();

        Matrix yRot = new Matrix();
        angle = (float) state.getyAng();
        yRot.toYRot(angle);

        Matrix zRot = new Matrix();
        angle = (float) state.getzAng();
        zRot.toZRot(angle);

        Matrix rMatrix = xRot.prodAndReturn(yRot.prodAndReturn(zRot));
        rMatrix.invert();
        Matrix fMatrix = cameraMatrix.prodAndReturn(rMatrix.prodAndReturn(transformMatrix));

        finalMatrix = new android.graphics.Matrix();
        finalMatrix.setValues(fMatrix.toFloatArray());
        return;

    }

    private Location getLocation(int x, int y) {
        double longDiff = (x * 1.0 - WIDTH / 2) / Math.pow(2, ZOOM);
        double latDiff = (HEIGHT / 2 - y * 1.0) / Math.pow(2, ZOOM);

        Location pixelLocation = new Location(currentLocation);
        pixelLocation.setLatitude(pixelLocation.getLatitude() + latDiff);
        pixelLocation.setLongitude(pixelLocation.getLongitude() + longDiff);

        return pixelLocation;
    }

    public int[] projectPixel() {
        // Get the new Coordinates of a Pixel
        return null;
    }
}
