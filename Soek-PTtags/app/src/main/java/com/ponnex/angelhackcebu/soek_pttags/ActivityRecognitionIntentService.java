package com.ponnex.angelhackcebu.soek_pttags;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by Jimbo Alvarez on 5/22/2016.
 */
public class ActivityRecognitionIntentService extends IntentService {
    protected static String TAG = "com.ponnex.angelhackcebu.soek_pttags.ActivityRecognitionIntentService";
    private BeaconManager beaconManager;
    private Region region;
    private boolean isScanning = false;
    private boolean isServiceReady = false;

    public ActivityRecognitionIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG + "_ARIS", "Created");

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);

                    Log.e("Nearest Beacon", nearestBeacon.getProximityUUID().toString() + "");

                    String[] plateHex;
                    plateHex = nearestBeacon.getProximityUUID().toString().split("-");

                    String b = plateHex[3] + plateHex[4];
                    byte[] bytes = hexStringToByteArray(b);

                    String plateNumber;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        plateNumber = new String(bytes, StandardCharsets.UTF_8);
                    } else {
                        plateNumber = new String(bytes, Charset.forName("UTF-8"));
                    }

                    stopScanning();
                    showNotification(plateNumber, "You are inside a vehicle with plate number " + plateNumber + ".");
                    Log.d("Notification Shown", "You are inside a vehicle with plate number " + plateNumber + ".");
                }
            }
        });

        region = new Region("ranged region", null, 3, 3);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                isServiceReady = true;

                if (isScanning) {
                    beaconManager.startRanging(region);
                }
            }
        });
    }

    public void startScanning() {
        beaconManager.startRanging(region);
        isScanning = true;
    }

    public void stopScanning() {
        beaconManager.stopRanging(region);
        isScanning = false;
    }

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l/2];
        for (int i = 0; i < l; i += 2) {
            data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            int confidence = mostProbableActivity.getConfidence();
            int activityType = mostProbableActivity.getType();

            String activityName = getNameFromType(activityType);
            Log.d(TAG + "_HAS RESULT -->", activityName + ", " + confidence + "% ");

            if (activityName.equals("In Vehicle")) {
                if (confidence >= 75) {
                    startScanning();
                }
            } else if (activityName.equals("Still") || activityName.equals("On Foot") || activityName.equals("On Bicycle") || activityName.equals("Running") || activityName.equals("Walking")) {
                if (confidence >= 50) {

                }
            } else {
                Log.d(TAG, "UNKNOWN OR TILTING");
            }
        }
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.TILTING:
                return "Tilting";
        }
        return "Unknown";
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG + "_ARIS", "Destroyed");
        super.onDestroy();
    }
}