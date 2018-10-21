package com.fuelspot.admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.admin.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.admin.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.admin.MainActivity.REQUEST_PERMISSION;
import static com.fuelspot.admin.MainActivity.currencyCode;
import static com.fuelspot.admin.MainActivity.getVariables;
import static com.fuelspot.admin.MainActivity.isSigned;
import static com.fuelspot.admin.MainActivity.userCountry;
import static com.fuelspot.admin.MainActivity.userCountryName;
import static com.fuelspot.admin.MainActivity.userDisplayLanguage;
import static com.fuelspot.admin.MainActivity.userUnit;
import static com.fuelspot.admin.MainActivity.userlat;
import static com.fuelspot.admin.MainActivity.userlon;

public class WelcomeActivity extends AppCompatActivity {

    RequestQueue requestQueue;
    SharedPreferences prefs;
    Button continueButton;
    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_login);

        // Window
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplication()).getDefaultTracker();
        t.setScreenName("Welcome");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        prefs = this.getSharedPreferences("AdminInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(WelcomeActivity.this);

        continueButton = findViewById(R.id.permissionButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]
                        {PERMISSIONS_STORAGE[0], PERMISSIONS_STORAGE[1], PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_PERMISSION);
            }
        });
    }

    private void Localization() {
        if (userlat != null && userlon != null) {
            if (userlat.length() > 0 && userlon.length() > 0) {
                Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geo.getFromLocation(Double.parseDouble(userlat), Double.parseDouble(userlon), 1);
                    if (addresses.size() > 0) {
                        userCountry = addresses.get(0).getCountryCode();
                        prefs.edit().putString("userCountry", userCountry).apply();

                        userCountryName = addresses.get(0).getCountryName();
                        prefs.edit().putString("userCountryName", userCountryName).apply();

                        userDisplayLanguage = Locale.getDefault().getDisplayLanguage();
                        prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                        Locale userLocale = new Locale(Locale.getDefault().getISO3Language(), addresses.get(0).getCountryCode());
                        currencyCode = Currency.getInstance(userLocale).getCurrencyCode();
                        prefs.edit().putString("userCurrency", currencyCode).apply();

                        switch (userCountry) {
                            // US GALLON COUNTRIES
                            case "BZ":
                            case "CO":
                            case "DO":
                            case "EC":
                            case "GT":
                            case "HN":
                            case "HT":
                            case "LR":
                            case "MM":
                            case "NI":
                            case "PE":
                            case "US":
                            case "SV":
                                userUnit = getString(R.string.unitSystem2);
                                break;
                            // IMPERIAL GALLON COUNTRIES
                            case "AI":
                            case "AG":
                            case "BS":
                            case "DM":
                            case "GD":
                            case "KN":
                            case "KY":
                            case "LC":
                            case "MS":
                            case "VC":
                            case "VG":
                                userUnit = getString(R.string.unitSystem3);
                                break;
                            default:
                                // LITRE COUNTRIES. REST OF THE WORLD.
                                userUnit = getString(R.string.unitSystem1);
                                break;
                        }
                        prefs.edit().putString("userUnit", userUnit).apply();
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                if (ActivityCompat.checkSelfPermission(WelcomeActivity.this, PERMISSIONS_LOCATION[0]) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WelcomeActivity.this, PERMISSIONS_LOCATION[1]) == PackageManager.PERMISSION_GRANTED) {
                    FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                userlat = String.valueOf(location.getLatitude());
                                userlon = String.valueOf(location.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();

                                Localization();

                                //Registration finished
                                isSigned = true;
                                prefs.edit().putBoolean("isSigned", isSigned).apply();
                                getVariables(prefs);

                                Toast.makeText(WelcomeActivity.this, getString(R.string.settings_saved), Toast.LENGTH_LONG).show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }, 2000);
                            } else {
                                LocationRequest mLocationRequest = new LocationRequest();
                                mLocationRequest.setInterval(5000);
                                mLocationRequest.setFastestInterval(1000);
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            }
                        }
                    });
                }
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
