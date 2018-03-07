package science.apolline.utils;


import android.content.Context;
import android.content.SharedPreferences;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sparow on 2/26/2018.
 */
public class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";
    private static final String PREFS_NAME ="mapCameraState";

    private SharedPreferences mapStatePrefs;

    private String mMapName;

    public MapStateManager(Context context, String mMapName) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mMapName = mMapName;
    }

    public void saveMapState(GoogleMap mapMie) {
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition position = mapMie.getCameraPosition();

        editor.putFloat(LATITUDE+"_"+mMapName, (float) position.target.latitude);
        editor.putFloat(LONGITUDE+"_"+mMapName, (float) position.target.longitude);
        editor.putFloat(ZOOM+"_"+mMapName, position.zoom);
        editor.putFloat(TILT+"_"+mMapName, position.tilt);
        editor.putFloat(BEARING+"_"+mMapName, position.bearing);
        editor.putInt(MAPTYPE+"_"+mMapName, mapMie.getMapType());
        editor.apply();
    }

    public CameraPosition getSavedCameraPosition() {
        double latitude = mapStatePrefs.getFloat(LATITUDE+"_"+mMapName, 0);
        if (latitude == 0)
            return null;
        double longitude = mapStatePrefs.getFloat(LONGITUDE+"_"+mMapName, 0);
        LatLng target = new LatLng(latitude, longitude);

        float zoom = mapStatePrefs.getFloat(ZOOM+"_"+mMapName, 0);
        float bearing = mapStatePrefs.getFloat(BEARING+"_"+mMapName, 0);
        float tilt = mapStatePrefs.getFloat(TILT+"_"+mMapName, 0);

        return new CameraPosition(target, zoom, tilt, bearing);
    }

    public int getSavedMapType() {
        return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
    }
}