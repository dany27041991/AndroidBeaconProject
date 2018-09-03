package com.softhings.localizer.com.softhings.localizer.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.softhings.localizer.wox.CallWox;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import com.google.gson.Gson;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

import com.softhings.localizer.lea.Extra;
import com.softhings.localizer.lea.Lea;
import com.softhings.localizer.lea.Payload;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.ACTION_INDOOR;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.ACTION_OUTDOOR;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.SECRET;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.PILOT;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.USER;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.DATA_SOURCE_OBTRUSIVE;
import static com.softhings.localizer.com.softhings.localizer.constants.Constants.datasourcetipe;


@SuppressWarnings("deprecation")
public class LocalizationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

        private static final String TAG = "LocalizationService";

    // run on another Thread to avoid crash
    private Handler startHandler;
    private Handler stopHandler;

    // timer handling
    private Timer mTimer = null;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private List<String> validBeaconMac;
    private List<Double> rssiValues;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean serviceEnabled;

    public static long SCAN_PERIOD;
    public static long INTER_SCAN_PERIOD;
    public static float MIN_DISTANCE;
    public static Map<String, String> homeBeacons;
    public static Map<String, String> outdoorBeacons;
    public static Map<LatLng, String> outdoorAreas;

    private CallWox callWoX;

    public LocalizationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("onCreate() Service");

        callWoX = new CallWox(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               afterSleep();
            }
        }, 5000);

    }

    public void afterSleep () {
        startHandler = new Handler();
        stopHandler = new Handler();
        validBeaconMac = new ArrayList<>();
        rssiValues = new ArrayList<>();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        buildGoogleApiClient();
        createLocationRequest();

        if (Build.VERSION.SDK_INT >= 21) {
            System.out.println("Setting BluetoothLeScanne...");
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
        }

        if (Build.VERSION.SDK_INT < 21) {

            mLeScanCallback =
                    new BluetoothAdapter.LeScanCallback() {
                        @Override
                        public void onLeScan(final BluetoothDevice device, int rssi,
                                             byte[] scanRecord) {
                            System.out.println("RESULT OLD SCANNING");
                            Log.i("onLeScan----------->>>>", "MAC: " + device.getAddress() + " RSSI: " + rssi
                                    + " Name: " + device.getName());
                            if (isValidBeacon(device.getAddress())) {
                                validBeaconMac.add(device.getAddress());
                                rssiValues.add(Integer.valueOf(rssi).doubleValue());
                            }
                        }
                    };
        } else {

            mScanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    System.out.println("RESULT NEW SCANNING");
                    Log.i("result-------->>>>>>>", "MAC: " + result.getDevice().getAddress() + " RSSI: " + result.getRssi());
                    BluetoothDevice btDevice = result.getDevice();

                    if (isValidBeacon(btDevice.getAddress())) {
                        validBeaconMac.add(btDevice.getAddress());
                        rssiValues.add(Integer.valueOf(result.getRssi()).doubleValue());
                    }
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Log.i("ScanResult - Results", sr.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.e("Scan Failed", "Error Code: " + errorCode);
                }
            };

        }

        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, INTER_SCAN_PERIOD);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceEnabled = true;
        return super.onStartCommand(intent, flags, startId);
    }

    private void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private boolean isValidBeacon(String address) {
        return homeBeacons.containsKey(address) || outdoorBeacons.containsKey(address);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            startHandler.post(new Runnable() {

                @Override
                public void run() {
                    System.out.println("Avvio scansione");

                    if (!mGoogleApiClient.isConnected() && serviceEnabled)
                        mGoogleApiClient.connect();

                    if (filters != null)
                        filters.clear();

                    scanLeDevice();
                }

            });
        }
    }

    private void scanLeDevice() {

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {

            stopHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        System.out.println("STOP OLD SCANNING");

                        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        System.out.println("STOP NEW SCANNING");

                        if (mLEScanner == null)
                            System.out.println("STOP NEW SCANNING ---->> BluetoothLeScanner null");
                        else
                            System.out.println("STOP NEW SCANNING ---->> BluetoothLeScanner NO null");

                        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
                            mLEScanner.stopScan(mScanCallback);

                    }
                    String nearestBeacon = calculateNearestBeacon();
                    validBeaconMac.clear();
                    rssiValues.clear();
                    String position = null;
                    if (nearestBeacon != null) {
                        if (mGoogleApiClient.isConnected())
                            mGoogleApiClient.disconnect();

                        if (homeBeacons.containsKey(nearestBeacon)) {
                            position = homeBeacons.get(nearestBeacon);

                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
                                String dataformattata = format.format(timestamp);
                                Gson gson = new Gson();
                                Lea lea = new Lea();
                                lea.setAction(ACTION_INDOOR);
                                lea.setUser(USER);
                                lea.setPilot(PILOT);
                                lea.setTimestamp(dataformattata);
                                lea.setDataSourceType(datasourcetipe);
                                lea.setLocation("eu:c4a:"+nearestBeacon);
                                lea.setPosition(position);

                                Extra extra = new Extra();
                                extra.setDataSourceObtrusive(DATA_SOURCE_OBTRUSIVE);
                                lea.setExtra(extra);

                                String istanceId = generate();
                                Payload payload = new Payload();
                                payload.setInstanceId(istanceId);
                                lea.setPayload(payload);

                                String json = gson.toJson(lea);
                                callWoX.sendToWox(json);


                        }
                        else if (outdoorBeacons.containsKey(nearestBeacon))
                            position = outdoorBeacons.get(nearestBeacon);

                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
                            String dataformattata = format.format(timestamp);
                            Gson gson = new Gson();
                            Lea lea = new Lea();
                            lea.setAction(ACTION_OUTDOOR);
                            lea.setUser(USER);
                            lea.setPilot(PILOT);
                            lea.setTimestamp(dataformattata);
                            lea.setDataSourceType(datasourcetipe);
                            lea.setLocation("eu:c4a:"+nearestBeacon);
                            lea.setPosition(position);

                            Extra extra = new Extra();
                            extra.setDataSourceObtrusive(DATA_SOURCE_OBTRUSIVE);
                            lea.setExtra(extra);

                            String istanceId = generate();
                            Payload payload = new Payload();
                            payload.setInstanceId(istanceId);
                            lea.setPayload(payload);

                            String json = gson.toJson(lea);
                            callWoX.sendToWox(json);

                        Log.i(TAG, "Invio broadcast "+position);
                        Intent broadcatIntent = new Intent();
                        broadcatIntent.setAction("com.softhings.localizer");
                        broadcatIntent.putExtra("position", position);
                        sendBroadcast(broadcatIntent);
                    } else {
                        if (!mGoogleApiClient.isConnected() && serviceEnabled)
                            mGoogleApiClient.connect();
                        if (mLastLocation != null) {
                            Intent broadcatIntent = new Intent();
                            broadcatIntent.setAction("com.softhings.localizer");

                            float[] distance = new float[3];
                            for (Map.Entry<LatLng, String> entry : outdoorAreas.entrySet())
                            {
                                Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                        entry.getKey().latitude, entry.getKey().longitude, distance);

                                if (distance[0] < MIN_DISTANCE) {
                                    position = entry.getValue();
                                    break;
                                }
                            }

                            if (position != null) {
                                broadcatIntent.putExtra("position", position);

                                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
                                    String dataformattata = format.format(timestamp);
                                    Gson gson = new Gson();
                                    Lea lea = new Lea();
                                    lea.setAction(ACTION_OUTDOOR);
                                    lea.setUser(USER);
                                    lea.setPilot(PILOT);
                                    lea.setTimestamp(dataformattata);
                                    lea.setDataSourceType(datasourcetipe);
                                    lea.setLocation("eu:c4a:" + nearestBeacon);
                                    lea.setPosition(position);

                                    Extra extra = new Extra();
                                    extra.setDataSourceObtrusive(DATA_SOURCE_OBTRUSIVE);
                                    lea.setExtra(extra);

                                    String istanceId = generate();
                                    Payload payload = new Payload();
                                    payload.setInstanceId(istanceId);
                                    lea.setPayload(payload);

                                    String json = gson.toJson(lea);
                                    callWoX.sendToWox(json);

                            }
                            else
                                broadcatIntent.putExtra("position", mLastLocation.getLatitude()
                                        + " " + mLastLocation.getLongitude());
                            sendBroadcast(broadcatIntent);

                                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
                                String dataformattata = format.format(timestamp);
                                Gson gson = new Gson();
                                Lea lea = new Lea();
                                lea.setAction(ACTION_OUTDOOR);
                                lea.setUser(USER);
                                lea.setPilot(PILOT);
                                lea.setTimestamp(dataformattata);
                                lea.setDataSourceType(datasourcetipe);
                                lea.setLocation("eu:c4a:" + nearestBeacon);
                                Double latitude = mLastLocation.getLatitude();
                                Double longitude = mLastLocation.getLongitude();
                                lea.setPosition(latitude.toString()+" "+longitude.toString());

                                Extra extra = new Extra();
                                extra.setDataSourceObtrusive(DATA_SOURCE_OBTRUSIVE);
                                lea.setExtra(extra);

                                String istanceId = generate();
                                Payload payload = new Payload();
                                payload.setInstanceId(istanceId);
                                lea.setPayload(payload);

                                String json = gson.toJson(lea);
                                callWoX.sendToWox(json);

                        }

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                System.out.println("START OLD SCANNING");
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                System.out.println("START NEW SCANNING");

                if (mLEScanner == null)
                    System.out.println("START NEW SCANNING ---->> BluetoothLeScanner null");
                else
                    System.out.println("START NEW SCANNING ---->> BluetoothLeScanner NO null");

                mLEScanner.startScan(filters, settings, mScanCallback);
            }

        }
    }

    private String calculateNearestBeacon() {

        List<String> finalBeaconMac = new ArrayList<>();
        List<Double> finalRssiValues = new ArrayList<>();

        for (int i=0; i < validBeaconMac.size(); i++) {
            if (!validBeaconMac.get(i).equals("")) {
                int occurence = 1;
                Double rssiSum = rssiValues.get(i);
                for (int j = i+1; j < validBeaconMac.size(); j++) {
                    if (validBeaconMac.get(i).equals(validBeaconMac.get(j))) {
                        occurence++;
                        rssiSum = rssiSum + rssiValues.get(j);
                        validBeaconMac.set(j, "");
                    }
                }
                Log.i(TAG, "OCCORRENZE " + String.valueOf(occurence));
                Log.i(TAG, "SOMMA " + String.valueOf(rssiSum));
                Log.i(TAG, "MEDIA " + String.valueOf(rssiSum / occurence));
                //if (occurence > 1) {
                    finalBeaconMac.add(validBeaconMac.get(i));
                    finalRssiValues.add(rssiSum / occurence);
                //}
            }
        }
        Log.i(TAG, "NUMERO DI BEACON "+String.valueOf(finalBeaconMac.size()));
        Log.i(TAG, "NUMERO DI RSSI "+String.valueOf(finalRssiValues.size()));
        double minAccuracy = 100;
        String nearestBeacon = null;
        if (finalBeaconMac.size() > 1) {
            for (int i = 0; i < finalBeaconMac.size(); i++) {
                Log.i(TAG, "ofihwefoih---- " + finalBeaconMac.get(i));
                double currentAccuracy = calculateAccuracy(-69, finalRssiValues.get(i));
                if (currentAccuracy < minAccuracy) {
                    minAccuracy = currentAccuracy;
                    nearestBeacon = finalBeaconMac.get(i);
                }
            }
        } else if (finalBeaconMac.size() == 1)
            nearestBeacon = finalBeaconMac.get(0);

        return nearestBeacon;
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy() Service");
        mTimer.cancel();
        mGoogleApiClient.disconnect();
        serviceEnabled = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else
            return (0.89976)*Math.pow(ratio,7.7095) + 0.111;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void setSCAN_PERIOD(long scan_period)
    {
        SCAN_PERIOD = scan_period;
    }
    public void setINTER_SCAN_PERIOD(long inter_scan_period)
    {
        INTER_SCAN_PERIOD = inter_scan_period;
    }
    public void setMIN_DISTANCE(float min_distance)
    {
        MIN_DISTANCE = min_distance;
    }
    public void setHomeBeacons(Map<String, String> home)
    {
        homeBeacons = home;
    }
    public void setOutdoorBeacons(Map<String, String> out)
    {
        outdoorBeacons = out;
    }
    public void setOutdoorAreas(Map<LatLng, String> outAreas)
    {
        outdoorAreas = outAreas;
    }

    public static String generate() {
        String uid = "";
        try {
            //Initialize SecureRandom
            //This is a lengthy operation, to be done only upon
            //initialization of the application
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");

            //generate a random number
            String randomNum = new Integer(prng.nextInt()).toString();

            //get its digest
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] result =  sha.digest(randomNum.getBytes());

            System.out.println("Random number: " + randomNum);
            System.out.println("Message digest: " + hexEncode(result));
            uid =  hexEncode(result);
        }
        catch (NoSuchAlgorithmException ex) {
            System.err.println(ex);
        } finally {
            return uid;
        }
    }

    /**
     * The byte[] returned by MessageDigest does not have a nice
     * textual representation, so some form of encoding is usually performed.
     *
     * This implementation follows the example of David Flanagan's book
     * "Java In A Nutshell", and converts a byte array into a String
     * of hex characters.
     *
     * Another popular alternative is to use a "Base64" encoding.
     */
    static private String hexEncode(byte[] aInput){
        StringBuilder result = new StringBuilder();
        char[] digits = {'0', '1', '2', '3', '4','5','6','7','8','9','a','b','c','d','e','f'};
        for (int idx = 0; idx < aInput.length; ++idx) {
            byte b = aInput[idx];
            result.append(digits[ (b&0xf0) >> 4 ]);
            result.append(digits[ b&0x0f]);
        }
        return result.toString();
    }
}
