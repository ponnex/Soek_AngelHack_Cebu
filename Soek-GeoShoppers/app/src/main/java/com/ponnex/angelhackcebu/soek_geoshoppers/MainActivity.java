package com.ponnex.angelhackcebu.soek_geoshoppers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.List;
import java.util.UUID;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by Jimbo Alvarez on 5/21/2016.
 */
public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private Dialog dialogPromo;
    private boolean isScanning = false;
    private boolean isServiceReady = false;
    private SupportAnimator revealAnimator, reverseAnimator;
    public static final String PREF_USER_FIRST_TIME = "user_first_time";
    boolean isUserFirstTime;
    private String networkSSID = "SOEK";
    private String networkPass = "tqbfjotld";
    private CarouselView carouselView;

    private int[] sampleImages = {R.drawable.shirt, R.drawable.nike_shoes, R.drawable.shirt, R.drawable.nike_shoes, R.drawable.shirt};

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isUserFirstTime = Boolean.valueOf(Utils.readSharedSetting(MainActivity.this, PREF_USER_FIRST_TIME, "true"));

        Intent introIntent = new Intent(MainActivity.this, OnBoardingActivity.class);
        introIntent.putExtra(PREF_USER_FIRST_TIME, isUserFirstTime);

        if (isUserFirstTime) startActivity(introIntent);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        carouselView = (CarouselView) findViewById(R.id.carouselView);
        carouselView.setPageCount(sampleImages.length);

        carouselView.setImageListener(imageListener);

        dialogPromo = new Dialog(this);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);

                    Log.e("Nearest Beacon", "Major: " + nearestBeacon.getMajor() + ", Minor: " + nearestBeacon.getMinor() + ", Rssi: " + nearestBeacon.getRssi() + ", MeasuredPower: " + nearestBeacon.getMeasuredPower());
                    Log.e("Estimate Distance: ", "Distance: " + calculateAccuracy(nearestBeacon.getMeasuredPower(), nearestBeacon.getRssi()));

                    if ((calculateAccuracy(nearestBeacon.getMeasuredPower(), nearestBeacon.getRssi()) < 0.8) && (nearestBeacon.getMajor() != 1)) {
                        //Open new Activity and pass extra for value(e.g. Major and Minor) then get data
                        stopScanning();

                        Intent intent = new Intent(MainActivity.this, ItemScanActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("major", String.valueOf(nearestBeacon.getMajor()));
                        intent.putExtra("minor", String.valueOf(nearestBeacon.getMinor()));
                        startActivity(intent);
                    }
                }
            }
        });

        region = new Region("ranged region", UUID.fromString("74278bda-b644-4520-8f0c-720eaf059935"), 2, null);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (floatingActionButton != null)
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isScanning) {
                        startScanning();
                    } else if (isScanning && isServiceReady) {
                        stopScanning();
                    }
                }
            });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageResource(sampleImages[position]);
        }
    };

    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
                return ssid;
            }
        }
        return "!connected";
    }

    private void showAlertDialogWifi(String title, String message) {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Granted", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        WifiConfiguration conf = new WifiConfiguration();
                        conf.SSID = "\"" + networkSSID + "\"";

                        conf.preSharedKey = "\""+ networkPass +"\"";

                        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(true);
                        wifiManager.addNetwork(conf);

                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        for( WifiConfiguration i : list ) {
                            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                                wifiManager.disconnect();
                                wifiManager.enableNetwork(i.networkId, true);
                                wifiManager.reconnect();

                                break;
                            }
                        }

                        //NEXT TASK: REPLACE THIS CODE
                        //Use AsynTask for getting discounts and promo datas from JSON-SERVER then display if the data is available
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!dialogPromo.isShowing()) {
                                    if(getCurrentSsid(getApplicationContext()).equals("\"" + networkSSID + "\"")) {
                                        showPromoDialog();
                                    }
                                }
                            }
                        }, 5000);
                        //Use AsynTask for getting discounts and promo info from JSON-SERVER then display if the data is available

                    }
                })
                .setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //show snackbar that user must be connected to the local network in order to use SOEK-GeoShoppers features.
                    }
                })
                .create();

        dialog.show();
    }

    public void startScanning() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        beaconManager.startRanging(region);
        isScanning = true;
    }

    public void stopScanning() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        beaconManager.stopRanging(region);
        isScanning = false;
    }

    public void showPromoDialog() {
        dialogPromo.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogPromo.setContentView(R.layout.dialog_popup);
        dialogPromo.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialogPromo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageButton dialogButton = (ImageButton)dialogPromo.findViewById(R.id.close_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reverseShow();
            }
        });

        dialogPromo.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                revealShow();
            }
        });

        dialogPromo.setCancelable(false);
        dialogPromo.show();
    }

    public void revealShow() {
        View myView = dialogPromo.findViewById(R.id.content);

        // get the starting point of the clipping circle
        int cx = myView.getLeft();
        int cy = myView.getTop();

        // get the final radius for the clipping circle
        int dx = Math.max(cx, myView.getWidth() - cx);
        int dy = Math.max(cy, myView.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        revealAnimator = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        revealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnimator.setDuration(500);
        reverseAnimator = revealAnimator.reverse();
        revealAnimator.start();
    }

    public void reverseShow() {
        reverseAnimator.setDuration(250);
        reverseAnimator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                dialogPromo.dismiss();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        reverseAnimator.start();
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

        //check first if already connected to the local network or not
        try {
            Log.e("Connected Network", getCurrentSsid(getApplicationContext()) + "");
            if(!getCurrentSsid(getApplicationContext()).equals("\"" + networkSSID + "\"") || (getCurrentSsid(getApplicationContext()).equals("\"" + "!connected" + "\""))) {
                Log.e("Connected Network", "Connected!");
                showAlertDialogWifi("Connect to Soek Wifi Portal", "Soek needs your permission to connect you to STORE local server to enjoy its functionalities.");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ponnex.angelhackcebu.soek_geoshoppers/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.ponnex.angelhackcebu.soek_geoshoppers/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}

