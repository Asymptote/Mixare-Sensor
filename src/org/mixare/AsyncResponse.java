package org.mixare;

import org.mapsforge.core.model.LatLong;

import java.util.List;

/**
 * Created by MelanieW on 03.02.2016.
 */
public interface AsyncResponse {
    void processFinish( List<LatLong> latLong);
}