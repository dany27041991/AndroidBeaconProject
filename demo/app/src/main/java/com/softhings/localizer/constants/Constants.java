package com.softhings.localizer.com.softhings.localizer.constants;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Constants {

    public final static String  ACTION_INDOOR = "eu:c4a:POI_ENTER";
    public final static String  ACTION_OUTDOOR = "eu:c4a:POI_EXIT";
    public final static String  SECRET = "token";
    public final static String  PILOT = "LCC";
    public final static String  USER = "eu:c4a:12345:";
    public final static Boolean  DATA_SOURCE_OBTRUSIVE = false;
    public final static  List<String> datasourcetipe = new ArrayList<String>(Arrays.asList("sensors", "external_dataset"));
    public final static String UrlRest = "https://gist.githubusercontent.com/anonymous/f99a411053c5b910ed92ccc12a4cf743/raw/e4bbff189a462a5ca853d9ad6054385cf42bfef1/file4Cfjson.json";

    public static final long SCAN_PERIOD = 5000;
    public static final long INTER_SCAN_PERIOD = 7 * 1000;
    public static final Map<String, String> homeBeacons;
    static
    {
        homeBeacons = new HashMap<>();
        homeBeacons.put("D9:87:36:2A:34:EC", "ROOM 1");
        homeBeacons.put("D5:48:D8:63:FC:91", "ROOM 2");
    }
    public static final Map<String, String> outdoorBeacons;
    static
    {
        outdoorBeacons = new HashMap<>();
        outdoorBeacons.put("XXX", "PHARMACY");
        outdoorBeacons.put("XXX", "MARKET");
    }
    public static final Map<LatLng, String> outdoorAreas;
    static
    {
        outdoorAreas = new HashMap<>();
        outdoorAreas.put(new LatLng(40.3343160, 18.1131320), "PARK");
        outdoorAreas.put(new LatLng(40.335304, 18.114794), "SQUARE");
    }

    public static final float MIN_DISTANCE = 50;
}
