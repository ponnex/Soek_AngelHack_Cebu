package com.ponnex.angelhackcebu.soek_pttags;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_report);

        TextView textView = (TextView)findViewById(R.id.plate_number);
        if (textView != null)
            textView.setText(intent.getStringExtra("plate_number"));
    }
}
