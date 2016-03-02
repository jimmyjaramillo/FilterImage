package 0.1;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.ExifInterface;
import android.location.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVFilterImage extends CordovaPlugin {
    private JSONObject params;
    private String imgDecodableString;
    private String FileString = "";
    private String Exif = "";
    private ArrayList<String> listPathFile = new ArrayList<String>();
    private String listGeo ;
    private Integer typeFilter;
    private Integer lat;
    private Integer log;
    private Integer rad;
    private String latitude;
    private String longitude;
    private String latitudeRef;
    private String longitudeRef;
    private String Latitude;
    private Float Longitude;
    private Date dateStar;
    private Date dateFinish;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            this.params = args.getJSONObject(0);
            loadImagefromGallery();
            this.coolMethod(listPathFile, callbackContext);
            return true;
        }
        return false;
    }

    public void loadImagefromGallery() throws IOException {
        File file = new File ("/storage/sdcard0/DCIM");
        typeFilter = valueFilter(this.params.DataTimeStart, this.params.DataTimeFinish, this.params.Latitude, this.params.Longitude, this.params.Radius);
        systemFile(file.getAbsolutePath());
    }

    public static Integer valueFilter (Date dateStar, Date dateFinish, Integer lat, Integer log, Integer rad) {

        if (dateStar != null && dateFinish != null && lat != null && log != null && rad != null) {
            return 1;
        }
        if (dateStar != null && dateFinish != null && lat == null && log == null && rad == null) {
            return 2;
        }
        if (dateStar == null && dateFinish == null && lat != null && log != null && rad != null) {
            return 3;
        }
        if (dateStar == null && dateFinish == null && lat == null && log == null && rad == null) {
            return 4;
        }
        return 0;
    }

    private void systemFile (String pathFile) {
        File currentDirectory = new File(pathFile);
        File[] listFile = currentDirectory.listFiles();
        for(File file : listFile) {
            if (file.isFile() && ((file.getPath().indexOf(".jpg") > -1) || (file.getPath().indexOf(".JPG") > -1))) {
                try {
                    ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                    latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                    longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                    latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                    longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                    if (latitude != null && longitude != null) {
                        calculateGeolocation(latitude, longitude, latitudeRef, longitudeRef);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                listPathFile.add(file.getPath());
            }else{
                if (!((file.getPath().indexOf(".thumbnails") > -1) || (file.getPath().indexOf("Facebook") > -1) || (file.getPath().indexOf("browser-photos") > -1))) {
                    systemFile(file.getAbsolutePath());
                }
            }
        }
    }

    public float calculateGeolocation (String Latitude1, String Longitude1, String latitudeR, String longitudeR) {
        String[] valueLatitude = Latitude1.split(",");
        String[] valueLongitude = Longitude1.split(",");
        String valueLat;
        String valueLong;
        String[] valLat;
        String[] valLong;
        float divLat;
        float divLong;
        float sumLat = 0;
        float sumLong = 0;
        for (int i = 0; i<valueLatitude.length; i++) {
            valueLat = valueLatitude[i];
            valLat = valueLat.split("/");
            valueLong = valueLongitude[i];
            valLong = valueLong.split("/");
            if (i == 0) {
                divLat = Float.parseFloat(valLat[0])/Float.parseFloat(valLat[1]);
                divLong = Float.parseFloat(valLong[0])/Float.parseFloat(valLong[1]);

            }else {
                if (i == 1) {
                    divLat = (Float.parseFloat(valLat[0])/Float.parseFloat(valLat[1]))/60;
                    divLong = (Float.parseFloat(valLong[0])/Float.parseFloat(valLong[1]))/60;
                }else {
                    divLat = (Float.parseFloat(valLat[0])/Float.parseFloat(valLat[1]))/3600;
                    divLong = (Float.parseFloat(valLong[0])/Float.parseFloat(valLong[1]))/3600;
                }
            }
            sumLat = sumLat + divLat;
            sumLong = sumLong + divLong;
        }
        if (latitudeR.equals("S")){
            sumLat = -1 * sumLat;
        }
        if (longitudeR.equals("W")){
            sumLong = -1 * sumLong;
        }
        Location loc1 = new Location("");
        loc1.setLatitude(sumLat);
        loc1.setLongitude(sumLong);
        Location loc2 = new Location("");
        loc2.setLatitude(-4.007891);
        loc2.setLongitude(-79.211277);
        float distanceInMeters = loc1.distanceTo(loc2);
        return 0;
    }

    private void coolMethod(ArrayList<String> listImages, CallbackContext callbackContext) {
        if (listImages.size() > 0) {
            callbackContext.success(listImages);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
