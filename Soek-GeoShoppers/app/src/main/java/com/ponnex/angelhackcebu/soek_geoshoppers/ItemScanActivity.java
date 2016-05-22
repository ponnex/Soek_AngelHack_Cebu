package com.ponnex.angelhackcebu.soek_geoshoppers;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by Jimbo Alvarez on 5/21/2016.
 */
public class ItemScanActivity extends AppCompatActivity {

    private FloatingActionButton fab_select, fab_set, fab_orange, fab_red, fab_black, fab_blue, fab_green, fab_xsmall, fab_small, fab_medium, fab_large, fab_xlarge;
    private FrameLayout frameLayout;
    private SupportAnimator revealAnimator, reverseAnimator;
    private Animation fab_grow;
    private String minor, local_url = "http://192.168.1.2:3000/", image_url = "", name_url = "", price_url = "", desc_url = "", quantity_url = "";
    private TextDrawable xsmall, small, medium, large, xlarge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_scan);

        final Intent intent = getIntent();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        minor = intent.getStringExtra("minor");

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            //getSupportActionBar().setTitle(intent.getStringExtra("major")  + "; " + intent.getStringExtra("minor"));
        }

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()){
                    collapsingToolbarLayout.setTitle(name_url);
                } else if (verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("");
                }
            }
        });

        new JSONParse().execute();

        ImageView addcart = (ImageView)findViewById(R.id.addcart);
        addcart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ItemScanActivity.this, PayActivity.class);
                startActivity(intent1);
            }
        });

        xsmall = new TextDrawable(this);
        xsmall.setText("XS");
        xsmall.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        small = new TextDrawable(this);
        small.setText("S");
        small.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        medium = new TextDrawable(this);
        medium.setText("M");
        medium.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        large = new TextDrawable(this);
        large.setText("L");
        large.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        xlarge = new TextDrawable(this);
        xlarge.setText("XL");
        xlarge.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        TextDrawable fabSelect = new TextDrawable(this);
        fabSelect.setText("M");
        fabSelect.setTextColor(Color.WHITE);
        fabSelect.setTextAlign(Layout.Alignment.ALIGN_CENTER);

        frameLayout = (FrameLayout)findViewById(R.id.selector_container);
        fab_set = (FloatingActionButton)findViewById(R.id.select_fab);

        fab_select = (FloatingActionButton) findViewById(R.id.fab);
        if (fab_select != null)
            fab_select.setImageDrawable(fabSelect);
            fab_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fab_select.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                        @Override
                        public void onHidden(FloatingActionButton fab) {
                            super.onHidden(fab);
                            fab_set.show();
                            revealAnimation();

                            xsmall.setTextColor(Color.BLACK);
                            small.setTextColor(Color.BLACK);
                            medium.setTextColor(Color.BLACK);
                            large.setTextColor(Color.BLACK);
                            xlarge.setTextColor(Color.BLACK);
                        }
                    });
                }
            });

        if(fab_set != null)
            fab_set.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fab_set.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                        @Override
                        public void onHidden(FloatingActionButton fab) {
                            super.onHidden(fab);
                            fab_select.show();
                            reverseAnimation();

                            xsmall.setTextColor(Color.WHITE);
                            small.setTextColor(Color.WHITE);
                            medium.setTextColor(Color.WHITE);
                            large.setTextColor(Color.WHITE);
                            xlarge.setTextColor(Color.WHITE);
                        }
                    });
                }
            });

        fab_grow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_grow);

        fab_orange = (FloatingActionButton)findViewById(R.id.fab_orange);
        if(fab_orange != null)
            fab_orange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABColor(fab_orange);
                }
            });

        fab_red = (FloatingActionButton)findViewById(R.id.fab_red);
        if(fab_red != null)
            fab_red.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABColor(fab_red);
                }
            });

        fab_black = (FloatingActionButton)findViewById(R.id.fab_black);
        if(fab_black != null)
            fab_black.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABColor(fab_black);
                }
            });

        fab_blue = (FloatingActionButton)findViewById(R.id.fab_blue);
        if(fab_blue != null)
            fab_blue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABColor(fab_blue);
                }
            });

        fab_green = (FloatingActionButton)findViewById(R.id.fab_green);
        if(fab_green != null)
            fab_green.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABColor(fab_green);
                }
            });

        fab_xsmall = (FloatingActionButton)findViewById(R.id.fab_xsmall);
        if(fab_xsmall != null)
            fab_xsmall.setImageDrawable(xsmall);
            fab_xsmall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABSize(fab_xsmall);
                }
            });

        fab_small = (FloatingActionButton)findViewById(R.id.fab_small);
        if(fab_small != null)
            fab_small.setImageDrawable(small);
            fab_small.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABSize(fab_small);
                }
            });

        fab_medium = (FloatingActionButton)findViewById(R.id.fab_medium);
        if(fab_medium != null)
            fab_medium.setImageDrawable(medium);
            fab_medium.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABSize(fab_medium);
                }
            });

        fab_large = (FloatingActionButton)findViewById(R.id.fab_large);
        if(fab_large != null)
            fab_large.setImageDrawable(large);
            fab_large.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABSize(fab_large);
                }
            });

        fab_xlarge = (FloatingActionButton)findViewById(R.id.fab_xlarge);
        if(fab_xlarge != null)
            fab_xlarge.setImageDrawable(xlarge);
            fab_xlarge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFABSize(fab_xlarge);
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab_select.show();
    }

    public void animateFABColor(FloatingActionButton floatingActionButton){
        if(floatingActionButton.equals(fab_orange)) {
            fab_orange.startAnimation(fab_grow);
            fab_red.clearAnimation();
            fab_black.clearAnimation();
            fab_blue.clearAnimation();
            fab_green.clearAnimation();
            fab_select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_dark)));
        } else if (floatingActionButton.equals(fab_red)) {
            fab_orange.clearAnimation();
            fab_red.startAnimation(fab_grow);
            fab_black.clearAnimation();
            fab_blue.clearAnimation();
            fab_green.clearAnimation();
            fab_select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)));
        } else if (floatingActionButton.equals(fab_black)) {
            fab_orange.clearAnimation();
            fab_red.clearAnimation();
            fab_black.startAnimation(fab_grow);
            fab_blue.clearAnimation();
            fab_green.clearAnimation();
            fab_select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), android.R.color.black)));
        } else  if (floatingActionButton.equals(fab_blue)) {
            fab_orange.clearAnimation();
            fab_red.clearAnimation();
            fab_black.clearAnimation();
            fab_blue.startAnimation(fab_grow);
            fab_green.clearAnimation();
            fab_select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_blue_bright)));
        } else if (floatingActionButton.equals(fab_green)) {
            fab_orange.clearAnimation();
            fab_red.clearAnimation();
            fab_black.clearAnimation();
            fab_blue.clearAnimation();
            fab_green.startAnimation(fab_grow);
            fab_select.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_dark)));
        } else {
            fab_orange.clearAnimation();
            fab_red.clearAnimation();
            fab_black.clearAnimation();
            fab_blue.clearAnimation();
            fab_green.clearAnimation();
        }
    }

    public void animateFABSize(FloatingActionButton floatingActionButton){
        if(floatingActionButton.equals(fab_xsmall)) {
            fab_xsmall.startAnimation(fab_grow);
            fab_small.clearAnimation();
            fab_medium.clearAnimation();
            fab_large.clearAnimation();
            fab_xlarge.clearAnimation();
            fab_select.setImageDrawable(xsmall);
        } else if (floatingActionButton.equals(fab_small)) {
            fab_xsmall.clearAnimation();
            fab_small.startAnimation(fab_grow);
            fab_medium.clearAnimation();
            fab_large.clearAnimation();
            fab_xlarge.clearAnimation();
            fab_select.setImageDrawable(small);
        }else if (floatingActionButton.equals(fab_medium)) {
            fab_xsmall.clearAnimation();
            fab_small.clearAnimation();
            fab_medium.startAnimation(fab_grow);
            fab_large.clearAnimation();
            fab_xlarge.clearAnimation();
            fab_select.setImageDrawable(medium);
        } else if (floatingActionButton.equals(fab_large)) {
            fab_xsmall.clearAnimation();
            fab_small.clearAnimation();
            fab_medium.clearAnimation();
            fab_large.startAnimation(fab_grow);
            fab_xlarge.clearAnimation();
            fab_select.setImageDrawable(large);
        } else if (floatingActionButton.equals(fab_xlarge)) {
            fab_xsmall.clearAnimation();
            fab_small.clearAnimation();
            fab_medium.clearAnimation();
            fab_large.clearAnimation();
            fab_xlarge.startAnimation(fab_grow);
            fab_select.setImageDrawable(xlarge);
        } else {
            fab_xsmall.clearAnimation();
            fab_small.clearAnimation();
            fab_medium.clearAnimation();
            fab_large.clearAnimation();
            fab_xlarge.clearAnimation();
        }

    }

    public void revealAnimation() {
        final View select_content = findViewById(R.id.selector_container);

        // get the starting point of the clipping circle
        int cx = select_content.getRight();
        int cy = select_content.getBottom();

        // get the final radius for the clipping circle
        int dx = Math.max(cx, select_content.getWidth() - cx);
        int dy = Math.max(cy, select_content.getHeight() - cy);
        float finalRadius = (float) Math.hypot(dx, dy);

        revealAnimator = ViewAnimationUtils.createCircularReveal(select_content, cx, cy, 0, finalRadius);
        revealAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        revealAnimator.setDuration(500);
        reverseAnimator = revealAnimator.reverse();
        revealAnimator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                frameLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {

            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        revealAnimator.start();
    }

    public void reverseAnimation() {
        reverseAnimator.setDuration(500);
        reverseAnimator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                frameLayout.setVisibility(View.INVISIBLE);
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


    private class JSONParse extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(String... args) {
            String urlString = local_url + "minor/" + minor;

            try{
                JSONObject jsonObject = getJSONObjectFromURL(urlString);
                return  jsonObject;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject json) {

            try {
                Log.e("JSONObject", json.toString());

                try {
                    image_url = json.getString("image");
                    name_url = json.getString("name");
                    price_url = json.getString("price");
                    desc_url = json.getString("desc");
                    quantity_url = json.getString("quantity");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TextView nameText = (TextView)findViewById(R.id.name);
                if (nameText != null)
                nameText.setText(name_url);

                TextView priceText = (TextView)findViewById(R.id.price);
                String priceString = "Php " + price_url;
                if (priceText != null)
                priceText.setText("Price: " + priceString);

                TextView descText = (TextView)findViewById(R.id.desc);
                if (descText != null)
                descText.setText("Description: " + desc_url);

                TextView quantityText = (TextView)findViewById(R.id.quantity);
                if (quantityText != null)
                    quantityText.setText("Quantity Left: " + quantity_url);

                String url = local_url + image_url;
                Log.e("shoes", url);

                ImageView imageView = (ImageView)findViewById(R.id.product_image);
                Glide.with(ItemScanActivity.this)
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.shirt)
                        .crossFade()
                        .centerCrop()
                        .into(imageView);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {

        HttpURLConnection urlConnection = null;

        URL url = new URL(urlString);

        urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);

        urlConnection.setDoOutput(true);

        urlConnection.connect();

        BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));

        char[] buffer = new char[1024];

        String jsonString = new String();

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();

        jsonString = sb.toString();

        Log.e("JSON: ", jsonString);

        return new JSONObject(jsonString);
    }
}
