package com.softhings.localizer.com.softhings.localizer.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Resources;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.softhings.localizer.R;
import com.softhings.localizer.com.softhings.localizer.services.LocalizationService;


//import com.softhings.localizer.parsing.CityZone;
//import com.softhings.localizer.parsing.HomeBeacons;
//import com.softhings.localizer.parsing.POIBeacons;
//import com.softhings.localizer.parsing.Result;
//import com.softhings.localizer.sharedpreference.SharedPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.softhings.localizer.estimote.BeaconID;
import com.softhings.localizer.estimote.EstimoteCloudBeaconDetails;
import com.softhings.localizer.estimote.EstimoteCloudBeaconDetailsFactory;
import com.softhings.localizer.estimote.ProximityContentManager;
import com.estimote.sdk.SystemRequirementsChecker;
import com.softhings.localizer.parsing.CityZone;
import com.softhings.localizer.parsing.HomeBeacons;
import com.softhings.localizer.parsing.POIBeacons;
import com.softhings.localizer.parsing.Result;
import com.softhings.localizer.sharedpreference.SharedPreference;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.UrlRest;

import com.softhings.localizer.lea.Payload;
import com.softhings.localizer.lea.Extra;
import com.softhings.localizer.lea.Lea;

import java.util.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    Activity context = this;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 3;
    private Button startBtn;
    private Button stopBtn;
    private TextView positionTv;
    private TextView serviceStatus;
    protected static final String TAG = "MainActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager lm;
    private AlertDialog locDialog;

    private ProximityContentManager proximityContentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        positionTv = (TextView) findViewById(R.id.positionTv);
        serviceStatus = (TextView) findViewById(R.id.serviceStatus);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        }

        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        serviceStatus.setTextColor(Color.RED);
        serviceStatus.setText(R.string.off_string);

        //Search Beacon in the area
        proximityContentManager = new ProximityContentManager(this,
                Arrays.asList(
                        // TODO: replace with UUIDs, majors and minors of your own beacons
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 1, 1),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 2, 2),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 3, 3)),
                new EstimoteCloudBeaconDetailsFactory());
        proximityContentManager.setListener(new ProximityContentManager.Listener() {
            @Override
            public void onContentChanged(Object content) {
                String text;
                if (content != null) {
                    EstimoteCloudBeaconDetails beaconDetails = (EstimoteCloudBeaconDetails) content;
                    text = "You're in " + beaconDetails.getBeaconName() + "'s range!";
                } else {
                    text = "No beacons in range.";
                }
                ((TextView) findViewById(R.id.textView)).setText(text);
            }
        });
        getJSON();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermAndIF();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            Log.d(TAG, "Starting ProximityContentManager content updates");
            proximityContentManager.startContentUpdates();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (intent != null) {
            String position = intent.getStringExtra("position");
            if (position != null)
                positionTv.setText(position);
        }



        super.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "coarse location permission granted");
                } else {
                    Log.i(TAG, "coarse location permission NOT granted");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        } else
            System.out.println("!!");

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startLocalization(View view) {
        Log.i(TAG, "Clicco start");
        startBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        serviceStatus.setTextColor(Color.GREEN);
        serviceStatus.setText(R.string.on_string);
        Intent intent = new Intent(this, LocalizationService.class);
        startService(intent);
    }

    public void stopLocalization(View view) {
        Log.i(TAG, "Clicco stop");
        stopBtn.setEnabled(false);
        startBtn.setEnabled(true);
        serviceStatus.setTextColor(Color.RED);
        serviceStatus.setText(R.string.off_string);
        Intent intent = new Intent(this, LocalizationService.class);
        stopService(intent);
    }

    public void checkPermAndIF() {

        if ((mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) ) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

        } else if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&*/ !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (locDialog != null)
                locDialog.cancel();
            locDialog = new AlertDialog.Builder(this)
                    .setTitle("This app needs location access")
                    .setMessage("Please grant location access so this app can detect beacons.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            MainActivity.this.startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
                        }
                    }).show();
            locDialog.setCancelable(false);
            System.out.println("DIALOG CREATO!!");

        }
    }

    public void getJSON()
    {
        //Get Json from url
        StringRequest request = new StringRequest(UrlRest, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                parseJsonData(string);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Failed to upload json files!!", Toast.LENGTH_SHORT).show();
                parseJsonDataOffline();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(MainActivity.this);
        rQueue.add(request);
    }

    void parseJsonData(String jsonString) {
        LocalizationService localizationService = new LocalizationService();
        //Parsing Json and SharedPreference
        SharedPreference sharedPreference = new SharedPreference();
        sharedPreference.saveString(context,jsonString);
        Gson gson = new Gson();
        Result result = gson.fromJson(jsonString, Result.class);
        //TextView txtshared = (TextView)findViewById(R.id.textView2);
        //txtshared.setText(jsonString);
        String stringa0 = result.getVersion();
        String stringa1 = result.getUserID();
        String stringa2 = result.getUUID();
        String stringa3 = result.getScanPeriod();
        localizationService.setSCAN_PERIOD(Long.parseLong(stringa3));
        String stringa4 = result.getInterScanPeriod();
        localizationService.setINTER_SCAN_PERIOD(Long.parseLong(stringa4));
        String stringa5 = result.getRay();
        localizationService.setMIN_DISTANCE(Float.parseFloat(stringa5));
        String stringa6 = "";
        if(result != null)
        {
            Map<String, String> homeBeacons = new HashMap<>();
            for(HomeBeacons j : result.getHomeBeacons())
            {
                stringa6 = stringa6.concat(j.getRoom() + ",\n" +j.getMacAddress() + "\n");
                homeBeacons.put(j.getMacAddress(), j.getRoom());
            }
            localizationService.setHomeBeacons(homeBeacons);
        }
        String stringa7 = "";
        if(result != null)
        {
            Map<String, String> outdoorBeacons = new HashMap<>();
            for(POIBeacons k : result.getPOIBeacons())
            {
                stringa7 = stringa7.concat(k.getPlace() + ",\n" + k.getMacAddress() + "\n");
                outdoorBeacons.put(k.getMacAddress(), k.getPlace());
            }
            localizationService.setOutdoorBeacons(outdoorBeacons);
        }
        String stringa8 = "";
        if(result != null)
        {
            Map<LatLng, String> outdoorAreas = new HashMap<>();
            for(CityZone t : result.getCityZones())
            {
                stringa8 = stringa8.concat(t.getName() + ",\n" + t.getCenter() + "\n");
                String[] latlong =  t.getCenter().split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                outdoorAreas.put(new LatLng(latitude, longitude), t.getName());
            }
            localizationService.setOutdoorAreas(outdoorAreas);
        }
        sharedPreference.save(context, stringa0, stringa1, stringa2, stringa3, stringa4
                , stringa5, stringa6, stringa7, stringa8);
        //txtshared.setText(sharedPreference.getValueUUID(context));
    }

    void parseJsonDataOffline() {
        LocalizationService localizationService = new LocalizationService();
        SharedPreference sharedPreference = new SharedPreference();
        String text = sharedPreference.getJsonString(context);
        if(text != null)
        {
            Gson gson = new Gson();
            Result result = gson.fromJson(text, Result.class);
            //TextView txtshared = (TextView)findViewById(R.id.textView2);
            //txtshared.setText(jsonString);
            String stringa0 = result.getVersion();
            String stringa1 = result.getUserID();
            String stringa2 = result.getUUID();
            String stringa3 = result.getScanPeriod();
            localizationService.setSCAN_PERIOD(Long.parseLong(stringa3));
            String stringa4 = result.getInterScanPeriod();
            localizationService.setINTER_SCAN_PERIOD(Long.parseLong(stringa4));
            String stringa5 = result.getRay();
            localizationService.setMIN_DISTANCE(Float.parseFloat(stringa5));
            String stringa6 = "";
            if(result != null)
            {
                Map<String, String> homeBeacons = new HashMap<>();
                for(HomeBeacons j : result.getHomeBeacons())
                {
                    stringa6 = stringa6.concat(j.getRoom() + ",\n" +j.getMacAddress() + "\n");
                    homeBeacons.put(j.getMacAddress(), j.getRoom());
                }
                localizationService.setHomeBeacons(homeBeacons);
            }
            String stringa7 = "";
            if(result != null)
            {
                Map<String, String> outdoorBeacons = new HashMap<>();
                for(POIBeacons k : result.getPOIBeacons())
                {
                    stringa7 = stringa7.concat(k.getPlace() + ",\n" + k.getMacAddress() + "\n");
                    outdoorBeacons.put(k.getMacAddress(), k.getPlace());
                }
                localizationService.setOutdoorBeacons(outdoorBeacons);
            }
            String stringa8 = "";
            if(result != null)
            {
                Map<LatLng, String> outdoorAreas = new HashMap<>();
                for(CityZone t : result.getCityZones())
                {
                    stringa8 = stringa8.concat(t.getName() + ",\n" + t.getCenter() + "\n");
                    String[] latlong =  t.getCenter().split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    outdoorAreas.put(new LatLng(latitude, longitude), t.getName());
                }
                localizationService.setOutdoorAreas(outdoorAreas);
            }
        }
        else
        {
            Resources res = getResources();
            InputStream is = res.openRawResource(R.raw.json_file);
            Scanner scanner = new Scanner(is);
            StringBuilder builder = new StringBuilder();
            while(scanner.hasNextLine())
            {
                builder.append(scanner.nextLine());
            }
            String string = builder.toString();
            Gson gson = new Gson();
            Result result = gson.fromJson(string, Result.class);
            //TextView txtshared = (TextView)findViewById(R.id.textView2);
            //txtshared.setText(jsonString);
            String stringa0 = result.getVersion();
            String stringa1 = result.getUserID();
            String stringa2 = result.getUUID();
            String stringa3 = result.getScanPeriod();
            localizationService.setSCAN_PERIOD(Long.parseLong(stringa3));
            String stringa4 = result.getInterScanPeriod();
            localizationService.setINTER_SCAN_PERIOD(Long.parseLong(stringa4));
            String stringa5 = result.getRay();
            localizationService.setMIN_DISTANCE(Float.parseFloat(stringa5));
            String stringa6 = "";
            if(result != null)
            {
                Map<String, String> homeBeacons = new HashMap<>();
                for(HomeBeacons j : result.getHomeBeacons())
                {
                    stringa6 = stringa6.concat(j.getRoom() + ",\n" +j.getMacAddress() + "\n");
                    homeBeacons.put(j.getMacAddress(), j.getRoom());
                }
                localizationService.setHomeBeacons(homeBeacons);
            }
            String stringa7 = "";
            if(result != null)
            {
                Map<String, String> outdoorBeacons = new HashMap<>();
                for(POIBeacons k : result.getPOIBeacons())
                {
                    stringa7 = stringa7.concat(k.getPlace() + ",\n" + k.getMacAddress() + "\n");
                    outdoorBeacons.put(k.getMacAddress(), k.getPlace());
                }
                localizationService.setOutdoorBeacons(outdoorBeacons);
            }
            String stringa8 = "";
            if(result != null)
            {
                Map<LatLng, String> outdoorAreas = new HashMap<>();
                for(CityZone t : result.getCityZones())
                {
                    stringa8 = stringa8.concat(t.getName() + ",\n" + t.getCenter() + "\n");
                    String[] latlong =  t.getCenter().split(",");
                    double latitude = Double.parseDouble(latlong[0]);
                    double longitude = Double.parseDouble(latlong[1]);
                    outdoorAreas.put(new LatLng(latitude, longitude), t.getName());
                }
                localizationService.setOutdoorAreas(outdoorAreas);
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Stopping ProximityContentManager content updates");
        proximityContentManager.stopContentUpdates();
    }

    @Override
    protected void onDestroy() {
//        Intent intent = new Intent(this, LocalizationService.class);
//        stopService(intent);
        super.onDestroy();
        proximityContentManager.destroy();
    }
}
