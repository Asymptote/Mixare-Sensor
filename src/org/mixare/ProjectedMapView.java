package org.mixare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.View;

import org.mixare.lib.render.Camera;
import org.mixare.mapimage.ProjectedMap;

/**
 * Created by Ammar on 3/15/2016.
 */
public class ProjectedMapView extends View {

    MixViewActivity mixViewActivity;
    ProjectedMap projectedMapImage;
    Bitmap staticMap = BitmapFactory.decodeResource(getResources(), R.drawable.staticmap);
    public ProjectedMapView(Context context) {
        super(context);

        projectedMapImage = new ProjectedMap();
        try {
            mixViewActivity = (MixViewActivity) context;
            mixViewActivity.killOnError();
        } catch (Exception ex) {
            mixViewActivity.doError(ex, MixViewActivity.GENERAL_ERROR);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        MixViewActivity.getPaintScreen().setCanvas(canvas);

        if (!mixViewActivity.getMarkerRenderer().isInited()) {
            mixViewActivity.getMarkerRenderer().init(MixViewActivity.getPaintScreen());
        }

        //Log.d("PROJECT_MAP", "Drawing Map");
        if (projectedMapImage != null) {
            projectedMapImage.setCurrentLocation(MixContext.getInstance().getLocationFinder().getCurrentLocation());
            Camera renderCamera = mixViewActivity.getMarkerRenderer().getCam();
            if (renderCamera != null) {
                MixContext.getInstance().getRM(renderCamera.transform);
                projectedMapImage.projectMap(renderCamera);
            }
//            Bitmap projectMap = projectedMapImage.getMap();
            Bitmap projectMap = staticMap;
            if (projectMap == null) return;

            //android.graphics.Matrix fm = projectedMapImage.getMatrix();
            //Bitmap bm = Bitmap.createBitmap(projectMap,0, 0, projectMap.getWidth(), projectMap.getHeight(),projectedMapImage.getMatrix(),true);
            //if (bm == null) return;
            //Log.d("PROJECT_MAP", "Applied matrix to projectMap");

            //MixVector marker = projectedMapImage.getcMarker();
            //canvas.drawBitmap(projectMap, marker.x - projectMap.getWidth()/2, marker.y - projectMap.getHeight()/2, null);

            canvas.drawBitmap(projectMap, projectedMapImage.getMatrix(), null);
            //canvas.drawBitmap(projectMap, projectedMapImage.getMatrix(), null);
        }
    }
}
