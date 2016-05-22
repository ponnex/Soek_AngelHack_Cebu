package com.ponnex.angelhackcebu.soek_pttags;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.skyfishjy.library.RippleBackground;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private boolean isScanning = false;
    private boolean isServiceReady = false;
    private RippleBackground rippleBackground;

    @TargetApi(19)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        setContentView(R.layout.activity_main);

        if (intent.getBooleanExtra("scan", false)) {
            stopScanning();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
        }

        startService(new Intent(MainActivity.this, CoreService.class));

        rippleBackground = (RippleBackground)findViewById(R.id.content_ripple);

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

                    Intent intent = new Intent(MainActivity.this, VehicleInfoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("plate_number", plateNumber);
                    startActivity(intent);
                }
            }
        });

        region = new Region("ranged region", null, 3, 3);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null)
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isScanning) {
                    startScanning();
                } else if (isScanning && isServiceReady) {
                    stopScanning();
                }
            }
        });
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
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
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
        if(!rippleBackground.isRippleAnimationRunning()) {
            rippleBackground.startRippleAnimation();
        }
        beaconManager.startRanging(region);
        isScanning = true;
    }

    public void stopScanning() {
        if(rippleBackground.isRippleAnimationRunning()) {
            rippleBackground.stopRippleAnimation();
        }
        beaconManager.stopRanging(region);
        isScanning = false;
    }

}
