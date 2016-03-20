package org.mixare.data;

import android.util.Log;

import org.mixare.marker.ImageMarker;
import org.mixare.marker.LocalMarker;
import org.mixare.marker.NavigationMarker;
import org.mixare.marker.POIMarker;
import org.mixare.marker.SocialMarker;

/**
 * Created  on 06.12.2015.
 */
public class MarkerBuilder {
    private String id;
    private String title;
    private double latitude;
    private double longitude;
    private double altitude;
    private String pageURL;
    private String imageURL;

    public String getImageURL() {
        return imageURL;
    }

    public MarkerBuilder setImageURL(String imageURL) {
        this.imageURL = imageURL;
        return this;
    }

    public String getId() {
        return id;
    }

    public MarkerBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MarkerBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public MarkerBuilder setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public MarkerBuilder setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public MarkerBuilder setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public String getPageURL() {
        return pageURL;
    }

    public MarkerBuilder setPageURL(String pageURL) {
        this.pageURL = pageURL;
        return this;
    }

    public DataSource.TYPE getType() {
        return type;
    }

    public MarkerBuilder setType(DataSource.TYPE type) {
        this.type = type;
        return this;
    }

    public int getColor() {
        return color;
    }

    public MarkerBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    private DataSource.TYPE type;

    public DataSource.DISPLAY getDisplayType() {
        return displayType;
    }

    public MarkerBuilder setDisplayType(DataSource.DISPLAY displayType) {
        this.displayType = displayType;
        return this;
    }

    private DataSource.DISPLAY displayType;

    private int color;

    public MarkerBuilder(){

    }

    public LocalMarker build(){
        DataSource.DISPLAY standardDisplayType;
//        Log.d(MixViewActivity.TAG, "build Marker at lat="+latitude+", lon="+longitude);

        LocalMarker newMarker=null;
        if(!isValid()){ //only check properties, not displayType
            return null;
        }
//        Log.d(MixViewActivity.TAG, "new marker at lat="+latitude+", lon="+longitude+" would be valid");

        switch (type){
            case WIKIPEDIA:
                standardDisplayType = DataSource.DISPLAY.CIRCLE_MARKER;
                break;
            case TWITTER:
                newMarker=new SocialMarker(id,title,latitude,longitude,altitude,pageURL,0,color);
                standardDisplayType = DataSource.DISPLAY.CIRCLE_MARKER;
                break;
            case OSM:
                standardDisplayType = DataSource.DISPLAY.NAVIGATION_MARKER;
                break;
            case MIXARE:
                standardDisplayType = DataSource.DISPLAY.CIRCLE_MARKER;
                break;
            case ARENA:
                standardDisplayType = DataSource.DISPLAY.CIRCLE_MARKER;
                break;
            case PANORAMIO:
                standardDisplayType = DataSource.DISPLAY.IMAGE_MARKER;
                break;
            default:
                standardDisplayType = DataSource.DISPLAY.NOTSET;
        }
        if(displayType==null){
            displayType = standardDisplayType;
        }
        if(!isValid(true)){ //check displayType as well, especially for imageURL
            return null;
        }
//        Log.d(MixViewActivity.TAG, "new marker at lat="+latitude+", lon="+longitude+" would be valid (even judging from the display type)");

        if(newMarker==null){ //no special marker Type set, so determine by displayType
            switch (displayType) {
                case CIRCLE_MARKER:
                    newMarker=new POIMarker(id,title,latitude,longitude,altitude,pageURL,0, color);
//                    Log.d(MixViewActivity.TAG, "new circle POI Marker created at lat="+latitude+", lon="+longitude);
                    break;
                case NAVIGATION_MARKER:
                    newMarker=new NavigationMarker(id,title,latitude,longitude,altitude,pageURL,0,color);
                    break;
                case IMAGE_MARKER:
                    newMarker=new ImageMarker(id,title,latitude,longitude,altitude,pageURL,0,color,"",imageURL);
                    break;
                default:
                    newMarker=new POIMarker(id,title,latitude,longitude,altitude,pageURL,0,color);
            }
        }
        return newMarker;
    }

    public boolean isValid(){
        return isValid(false);
    }

    public boolean isValid(boolean checkType){
        //do some basic checks
        if(checkType && displayType==null){
//            Log.d(MixViewActivity.TAG, "new marker wouldn't be valid because of displayType");

            return false;
        }
        if(id==null || id.isEmpty() || title==null || title.isEmpty() ){
//            Log.d(MixViewActivity.TAG, "new marker wouldn't be valid because of "+id+", "+title+", "+type);

            return false;
        }
        if(displayType == DataSource.DISPLAY.IMAGE_MARKER && (imageURL==null || imageURL.isEmpty() )){
            return false;
        }
        if(type == null){
            type= DataSource.TYPE.NOTSET;
        }
        return true;
    }
}
