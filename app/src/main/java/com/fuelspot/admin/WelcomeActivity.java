package com.fuelspot.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import static com.fuelspot.admin.MainActivity.PERMISSIONS_LOCATION;
import static com.fuelspot.admin.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.admin.MainActivity.REQUEST_PERMISSION;
import static com.fuelspot.admin.MainActivity.currencyCode;
import static com.fuelspot.admin.MainActivity.currencySymbol;
import static com.fuelspot.admin.MainActivity.getVariables;
import static com.fuelspot.admin.MainActivity.isSigned;
import static com.fuelspot.admin.MainActivity.mapDefaultStationRange;
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
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time_login);

        // Window
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefs = this.getSharedPreferences("AdminInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(WelcomeActivity.this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    synchronized (this) {
                        super.onLocationResult(locationResult);
                        Location locCurrent = locationResult.getLastLocation();
                        if (locCurrent != null) {
                            if (locCurrent.getAccuracy() <= mapDefaultStationRange * 2) {
                                userlat = String.valueOf(locCurrent.getLatitude());
                                userlon = String.valueOf(locCurrent.getLongitude());
                                prefs.edit().putString("lat", userlat).apply();
                                prefs.edit().putString("lon", userlon).apply();
                                MainActivity.getVariables(prefs);
                                Localization();
                            }
                        } else {
                            Toast.makeText(WelcomeActivity.this, getString(R.string.error_no_location), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        };

        continueButton = findViewById(R.id.permissionButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(WelcomeActivity.this, new String[]
                            {PERMISSIONS_STORAGE[0], PERMISSIONS_STORAGE[1], PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_PERMISSION);
                } else {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
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

                        Currency userParaSembolu = Currency.getInstance(currencyCode);
                        currencySymbol = userParaSembolu.getSymbol(userLocale);
                        prefs.edit().putString("userCurrencySymbol", currencySymbol).apply();

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

        //Registration finished
        isSigned = true;
        prefs.edit().putBoolean("isSigned", isSigned).apply();
        getVariables(prefs);

        Toast.makeText(WelcomeActivity.this, getString(R.string.settings_saved), Toast.LENGTH_LONG).show();
        Intent i = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                }
            } else {
                Snackbar.make(this.findViewById(R.id.mainContainer), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
