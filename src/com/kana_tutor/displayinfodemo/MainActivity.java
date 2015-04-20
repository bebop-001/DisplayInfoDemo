package com.kana_tutor.displayinfodemo;

import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String logTag = "MainActivity";
    private static DisplayMetrics displayMetrics;
    private static float SP, DP;

    private static int intFromString(Context c, String stringInt) {
        int rv = 0;
        String type = null, val = null;
        Pattern p = Pattern.compile("^(\\d+)(sp|dp)*$");
        Matcher m = p.matcher(stringInt);
        try {
            if(m.matches()) {
                type    = m.group(2);
                val     = m.group(1);
            }
            else
                throw new Exception("numeric exception:"
                    + stringInt +
                    " can be all digits or end with 'dp' or 'sp'");
            rv = Integer.parseInt(val);
            if (null != type) {
                if (type.equals("dp"))
                    rv *= DP;
                else
                    rv *= SP;
            }
            // scale value to sp.
        } catch (Exception e) {
            Log.wtf(logTag
                    , "Bad string int:\"" + stringInt 
                    + ", error=" + e.getMessage());
        }
        return rv;
    }

    private int button4Width, button5Width;
    // time stamp on downloaded package for build date.
    private static String stringBuildDate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /****************** Build date for package ************************/
        try {
            // to determine build time we use the timestamp of the
            // classes.dex file in the apk file.
            // NOTE: packageInfo.lastUpdateTime indicates install time
            ApplicationInfo ai = getPackageManager()
                .getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            zf.close();
            stringBuildDate = SimpleDateFormat.getInstance()
                .format(new java.util.Date(time));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        displayMetrics = getResources().getDisplayMetrics();
        SP = displayMetrics.scaledDensity;
        DP = displayMetrics.density;
        
        Button button1, button4, button5;
        button4Width = intFromString(this, "250dp");
        button5Width = intFromString(this, "250sp");

        button1 = (Button)findViewById(R.id.button1);
        button4 = (Button)findViewById(R.id.button4);
        button5 = (Button)findViewById(R.id.button5);

        button1.setWidth(250);
        button4.setWidth(button4Width);
        button5.setWidth(button5Width);
    }
    public void buttonOnClick(View view) {
        Button b = (Button)view;
        int id = view.getId();
        String text = b.getText().toString();
        int width = view.getWidth();
        EditText et = (EditText)findViewById(R.id.editText1);
        et.setText(text + ": " + width + " pixels");
    }
    private void showDisplayInfo() {
        /*
         * Display Metrics Fields:
         * public float  density       The logical density of the display.
         * public int    densityDpi    The screen density expressed as
         *                             dots-per-inch.
         * public float  scaledDensity A scaling factor for fonts displayed on
         *                             the display.
         * public int    heightPixels  The absolute height of the display in
         *                             pixels.
         * public int    widthPixels   The absolute width of the display in
         *                             pixels.
         * public float  xdpi          The exact physical pixels per inch of
         *                             the screen in the X dimension.
         * public float  ydpi          The exact physical pixels per inch of
         *                             the screen in the Y dimension.
         */
        Map <Integer, String> densityNames = new HashMap<Integer, String>();
        densityNames.put(DisplayMetrics.DENSITY_DEFAULT,    "DENSITY_DEFAULT");
        densityNames.put(DisplayMetrics.DENSITY_HIGH,       "DENSITY_HIGH");
        densityNames.put(DisplayMetrics.DENSITY_LOW,        "DENSITY_LOW");
        densityNames.put(DisplayMetrics.DENSITY_MEDIUM,     "DENSITY_MEDIUM");
        densityNames.put(DisplayMetrics.DENSITY_TV,         "DENSITY_TV");
        densityNames.put(DisplayMetrics.DENSITY_XHIGH,      "DENSITY_XHIGH");
        Display display = getWindowManager().getDefaultDisplay();
        Map <Integer, String> orientationNames = new HashMap<Integer, String>(){
            private static final long serialVersionUID = 1L;
            {
                put(Surface.ROTATION_0, "0 degrees");
                put(Surface.ROTATION_90, "90 degrees");
                put(Surface.ROTATION_180, "180 degrees");
                put(Surface.ROTATION_270, "270 degrees");
            }
        };
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        String displayDensity = (densityNames.keySet().contains(metrics.densityDpi))
                ? densityNames.get(metrics.densityDpi) : "UNRECOGNIZED";
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;
        float density = metrics.densityDpi;
        float scaledDensity = metrics.scaledDensity;
        // A density-independent pixel is equivalent to one physical pixel
        // on a 160 dpi screen.
                int widthDp = (int)(Math.ceil(widthPixels / density)) * 160;
                int heightDp = (int)(Math.ceil(heightPixels / density) * 160);
                int widthSp = (int)(Math.ceil(widthPixels / scaledDensity));
                int heightSp = (int)(Math.ceil(heightPixels / scaledDensity));
                float widthInches = (metrics.widthPixels/(1.0f * metrics.densityDpi));
                float heightInches = (metrics.heightPixels/(1.0f * metrics.densityDpi));
        AlertDialog d = new AlertDialog.Builder(this)
            .setMessage(
                String.format("Density: %d pix/inch: %s\n"
                      , metrics.densityDpi, displayDensity)
                + String.format("physical pixels: %dx%d\n"
                      , metrics.widthPixels, metrics.heightPixels)
                + String.format("Dimensions %3.2fx%3.2f, %3.2f inches\n"
                      , widthInches, heightInches
                      , Math.sqrt(
                          ((widthInches * widthInches)
                          + (heightInches * heightInches))))
                + String.format("DPI: %3.2f pix/in: %dx%d\n"
                      , density, widthDp, heightDp)
                + String.format("Scaled Density: %3.2f pix/in: %dx%d\n"
                      , scaledDensity, widthSp, heightSp)
               // sjs + String.format("User selected font scale: %s\n"
               //         , Settings.System.getS)
                + String.format("device: %s-%s"
                        , android.os.Build.MANUFACTURER, android.os.Build.PRODUCT)
            )
            .setTitle("Display characteristics")
            .setPositiveButton("OK", null)
            .show();
        TextView tv = (TextView)d.findViewById(android.R.id.message);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean rv = false;
        int itemId = item.getItemId();
        if (R.id.show_display_info == itemId) {
            showDisplayInfo();
        }
        else if (R.id.menu_about == itemId) {
            final TextView message = new TextView(this);
            String versionName = "missing";
            int versionCode = 0;
                try {
                    PackageInfo pInfo = getPackageManager()
                        .getPackageInfo(getPackageName(), 0);
                    versionName = pInfo.versionName;
                    versionCode = pInfo.versionCode;
                } catch (NameNotFoundException e) {}

            String htmlString = 
                    "Version (" + versionCode + ")" + versionName
                    + " build date:" + stringBuildDate + "<br>"
                    + "For more information see, the "
                    + "<a href=\"http://kana-tutor.com\">"
                    + "kana tutor</a> web site";
            message.setText(Html.fromHtml(htmlString));
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            // scale our icon to be 50x50 dp.
            Drawable dr = getResources().getDrawable(R.drawable.qmark);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            Drawable icon = new BitmapDrawable(getResources()
                , Bitmap.createScaledBitmap(bitmap
                    , (int)(50 * DP), (int)(50 * DP), true));
            AlertDialog ad = new AlertDialog.Builder(this)
                .setIcon(icon)
                .setView(message)
                .setTitle("about " + getString(R.string.app_name))
                .setPositiveButton("OK", null)
                .show();
        }
        if (rv)
            return rv;
        else
            return super.onOptionsItemSelected(item);
    }
    private static Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MainActivity.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


}
