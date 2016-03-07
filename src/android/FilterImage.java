package imageFilter;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This class echoes a string called from JavaScript.
 */
public class FilterImage extends CordovaPlugin {
    private JSONObject params;
    private ArrayList<String> listPathFile;
    private Integer resp;
    private Integer radEvent;
    private String latitude;
    private String longitude;
    private String latitudeR;
    private String longitudeR;
    private String time;
    private Date dateStart;
    private Date dateFinish;
    private String[] valueLatitude;
    private String[] valueLongitude;
    private String valueLat;
    private String valueLong;
    private String[] valLat;
    private String[] valLong;
    private Float divLat;
    private Float divLong;
    private Float sumLat;
    private Float sumLong;
    private Location locPhoto;
    private Location locEvent;
    private Integer geoPhoto;
    private Integer timePhoto;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
          listPathFile = new ArrayList<String>();
            this.params = args.getJSONObject(0);
            resp = valueFilter(this.params.getString("DataTimeStart"),
                               this.params.getString("DataTimeFinish"),
                               this.params.getString("Latitude"),
                               this.params.getString("Longitude"),
                               this.params.getString("Radius"));

            try {
              dateStart = convertStringToTimeStamp(this.params.getString("DataTimeStart"));
              dateFinish = convertStringToTimeStamp(this.params.getString("DataTimeFinish"));
            }catch(Exception e) {
              e.printStackTrace();
            }
            try {
              locEvent = new Location("");
              locEvent.setLatitude(Double.parseDouble(this.params.getString("Latitude")));
              locEvent.setLongitude(Double.parseDouble(this.params.getString("Longitude")));
              radEvent = Integer.parseInt(this.params.getString("Radius"));
            }catch(Exception e) {
              e.printStackTrace();
            }

            File file = new File ("/storage/sdcard0/DCIM");
            systemFile(file.getAbsolutePath());
            Log.d("","hola: "+listPathFile);
            this.coolMethod(listPathFile, callbackContext);
            return true;
        }
        return false;
    }

    public static Integer valueFilter (String dateStart, String dateFinish, String lat, String log, String rad) {
      if (dateStart != "null" && dateFinish != "null" && lat != "null" && log != "null" && rad != "null") {
        return 1;
      }
      if (dateStart != "null" && dateFinish != "null" && lat == "null" && log == "null" && rad == "null") {
        return 2;
      }
      if (dateStart == "null" && dateFinish == "null" && lat != "null" && log != "null" && rad != "null") {
        return 3;
      }
      if (dateStart == "null" && dateFinish == "null" && lat == "null" && log == "null" && rad == "null") {
        return 4;
      }
      return 0;
    }

    public Date convertStringToTimeStamp (String date) throws ParseException {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date timeStamp = dateFormat.parse(date);
      return timeStamp;
    }

    public Integer calculateTime (String urlFile) {
      try {
        ExifInterface exifInterface = new ExifInterface(urlFile);
        time = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        if (time == null) {
          return  0;
        }
        String [] timeString = time.split(" ");
        String timeFinish = timeString[0].replace(":", "-") + " " + timeString[1];
        Date aux = convertStringToTimeStamp(timeFinish);
        if (dateStart.before(aux) && dateFinish.after(aux)) {
          return 1;
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ParseException e) {
        e.printStackTrace();
      }
      return 0;
    }

    public Integer calculateGeolocation (String urlFile) {
      try {
        ExifInterface exifInterface = new ExifInterface(urlFile);
        latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
              latitudeR = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
              longitudeR = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (latitude == null) {
        return 0;
      }
      valueLatitude = latitude.split(",");
          valueLongitude = longitude.split(",");
          valueLat = "";
          valueLong = "";
          valLat = new String[0];
          valLong = new String[0];
          float sumLat = 0;
          float sumLong = 0;
          for (int i = 0; i < valueLatitude.length; i++) {
        valueLat = valueLatitude[i];
        valLat = valueLat.split("/");
        valueLong = valueLongitude[i];
        valLong = valueLong.split("/");
        switch (i) {
          case 0:
               divLat = Float.parseFloat(valLat[0])/Float.parseFloat(valLat[1]);
               divLong = Float.parseFloat(valLong[0])/Float.parseFloat(valLong[1]);
               break;
          case 1:
                         divLat = (Float.parseFloat(valLat[0])/Float.parseFloat(valLat[1]))/60;
                         divLong = (Float.parseFloat(valLong[0])/Float.parseFloat(valLong[1]))/60;
                         break;
                  case 2:
                         divLat = (Float.parseFloat(valLat[0])/Float.parseFloat(valLat[1]))/3600;
                         divLong = (Float.parseFloat(valLong[0])/Float.parseFloat(valLong[1]))/3600;
                         break;
        };
        sumLat = sumLat + divLat;
        sumLong = sumLong + divLong;
      }
      if (latitudeR.equals("S")) {
        sumLat = -1 * sumLat;
      }
      if (longitudeR.equals("W")) {
        sumLong = -1 * sumLong;
      }
      locPhoto = new Location("");
          locPhoto.setLatitude(sumLat);
          locPhoto.setLongitude(sumLong);
          float distanceInMeters = locPhoto.distanceTo(locEvent);
      if (distanceInMeters <= radEvent) {
        return 1;
      }
      return 0;
    }

    private void systemFile (String pathFile) {
      File currentDirectory = new File(pathFile);
      File[] listFile = currentDirectory.listFiles();
      for(File file : listFile) {
        if (file.isFile()) {
          switch (resp) {
            case 0:
                 break;
            case 1:
                 timePhoto = calculateTime(file.getAbsolutePath());
                 if (timePhoto == 1) {
                   geoPhoto = calculateGeolocation(file.getAbsolutePath());
                   if (geoPhoto == 1) {
                     listPathFile.add(file.getPath());
                   }
                 }
                 break;
            case 2:
                 timePhoto = calculateTime(file.getAbsolutePath());
                 if (timePhoto == 1) {
                   listPathFile.add(file.getPath());
                 }
                 break;
            case 3:
                 geoPhoto = calculateGeolocation(file.getAbsolutePath());
                 if (geoPhoto == 1) {
                   listPathFile.add(file.getPath());
                 }
                 break;
            case 4:
                 listPathFile.add(file.getPath());
                 break;
          }
        }else {
          systemFile(file.getAbsolutePath());
        }
      }
    }

    private void coolMethod(ArrayList<String> listImages, CallbackContext callbackContext) {
        if (listImages.size() > 0) {
          JSONArray res = new JSONArray(listImages);
            callbackContext.success(res);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}