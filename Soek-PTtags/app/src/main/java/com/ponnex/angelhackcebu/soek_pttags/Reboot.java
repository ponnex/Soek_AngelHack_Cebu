package com.ponnex.angelhackcebu.soek_pttags;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Jimbo Alvarez on 5/22/2016.
 */
public class Reboot extends BroadcastReceiver {

    private String TAG = "com.ponnex.angelhackcebu.soek_pttags.Reboot";

    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG,"reboot");
        Intent intent1 = new Intent(context, CoreService.class);
        context.startService(intent1);

        Toast toast;
        toast = Toast.makeText(context, "Starting Soek PTtags...", Toast.LENGTH_LONG);
        toast.show();

        Log.e(TAG, "Starting Soek PTtags");
    }
}