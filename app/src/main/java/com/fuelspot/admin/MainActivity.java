package com.fuelspot.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Map;

import eu.amirs.JSON;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSION = 0;

    // Diameter of 50m circle
    public static int mapDefaultStationRange = 50;
    public static float mapDefaultZoom = 16f;

    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static boolean isSigned, isVerified, doubleBackToExitPressedOnce;

    public static String userPhoneNumber, userlat, userlon, name, email, password, photo, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, username, userUnit;

    Window window;
    Toolbar toolbar;
    RequestQueue requestQueue;
    SharedPreferences prefs;
    MapView mMapView;
    Location locLastKnown;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    GoogleMap googleMap;
    FusedLocationProviderClient mFusedLocationClient;
    Circle circle;

    // Current station information
    String stationName, stationVicinity, stationLocation, lastUpdated, stationLogo, placeID;
    int stationDistance, stationID;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;

    EditText stationNameHolder, stationAddressHolder, gasolineHolder, dieselHolder, lpgHolder, electricityHolder;
    Button buttonUpdateStation;
    BitmapDescriptor verifiedIcon;

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public static void getVariables(SharedPreferences prefs) {
        name = prefs.getString("Name", "");
        email = prefs.getString("Email", "");
        password = prefs.getString("password", "");
        photo = prefs.getString("ProfilePhoto", "http://fuel-spot.com/FUELSPOTAPP/default_icons/profile.png");
        gender = prefs.getString("Gender", "");
        birthday = prefs.getString("Birthday", "");
        location = prefs.getString("Location", "");
        username = prefs.getString("UserName", "");
        userlat = prefs.getString("lat", "39.925054");
        userlon = prefs.getString("lon", "32.8347552");
        isSigned = prefs.getBoolean("isSigned", false);
        userCountry = prefs.getString("userCountry", "");
        userCountryName = prefs.getString("userCountryName", "");
        userDisplayLanguage = prefs.getString("userLanguage", "");
        userUnit = prefs.getString("userUnit", "");
        currencyCode = prefs.getString("userCurrency", "");
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Window
        window = this.getWindow();

        // Initializing Toolbar and setting it as the actionbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setLogo(R.drawable.brand_logo);
        }

        coloredBars(Color.parseColor("#616161"), Color.parseColor("#ffffff"));
        prefs = getSharedPreferences("AdminInformation", Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        fetchAccount();

        // Activate map
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        MapsInitializer.initialize(this.getApplicationContext());
        verifiedIcon = BitmapDescriptorFactory.fromResource(R.drawable.verified_station);

        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        locLastKnown = new Location("");
        locLastKnown.setLatitude(Double.parseDouble(userlat));
        locLastKnown.setLongitude(Double.parseDouble(userlon));

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                synchronized (this) {
                    super.onLocationResult(locationResult);
                    Location locCurrent = locationResult.getLastLocation();
                    if (locCurrent != null) {
                        if (locCurrent.getAccuracy() <= mapDefaultStationRange * 10) {
                            userlat = String.valueOf(locCurrent.getLatitude());
                            userlon = String.valueOf(locCurrent.getLongitude());
                            prefs.edit().putString("lat", userlat).apply();
                            prefs.edit().putString("lon", userlon).apply();
                            MainActivity.getVariables(prefs);

                            float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                            if (distanceInMeter >= mapDefaultStationRange) {
                                locLastKnown.setLatitude(Double.parseDouble(userlat));
                                locLastKnown.setLongitude(Double.parseDouble(userlon));
                                updateMapObject();
                            }
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.mainContainer), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        };


        // Layout items
        stationNameHolder = findViewById(R.id.editTextStationName);
        stationNameHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    stationName = s.toString();
                }
            }
        });
        stationAddressHolder = findViewById(R.id.editTextStationAddress);
        stationAddressHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    stationVicinity = s.toString();
                }
            }
        });
        gasolineHolder = findViewById(R.id.editTextGasoline);
        gasolineHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    gasolinePrice = Float.parseFloat(s.toString());
                }
            }
        });
        dieselHolder = findViewById(R.id.editTextDiesel);
        dieselHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    dieselPrice = Float.parseFloat(s.toString());
                }
            }
        });
        lpgHolder = findViewById(R.id.editTextLPG);
        lpgHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    lpgPrice = Float.parseFloat(s.toString());
                }
            }
        });
        electricityHolder = findViewById(R.id.editTextElectricity);
        electricityHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    electricityPrice = Float.parseFloat(s.toString());
                }
            }
        });

        buttonUpdateStation = findViewById(R.id.buttonUpdate);
        buttonUpdateStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stationName != null && stationName.length() > 0) {
                    if (stationVicinity != null && stationVicinity.length() > 0) {
                        updateStation();
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.stationAddressEmpty), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.stationNameEmpty), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void fetchAccount() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_LOGIN),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "Fail":
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_fail), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                                break;
                            default:
                                try {
                                    JSONArray res = new JSONArray(response);
                                    JSONObject obj = res.getJSONObject(0);

                                    username = obj.getString("username");
                                    prefs.edit().putString("UserName", username).apply();

                                    name = obj.getString("name");
                                    prefs.edit().putString("Name", name).apply();

                                    email = obj.getString("email");
                                    prefs.edit().putString("Email", email).apply();

                                    password = obj.getString("password");
                                    prefs.edit().putString("password", password).apply();

                                    photo = obj.getString("photo");
                                    prefs.edit().putString("ProfilePhoto", photo).apply();

                                    gender = obj.getString("gender");
                                    prefs.edit().putString("Gender", gender).apply();

                                    birthday = obj.getString("birthday");
                                    prefs.edit().putString("Birthday", birthday).apply();

                                    userPhoneNumber = obj.getString("phoneNumber");
                                    prefs.edit().putString("userPhoneNumber", userPhoneNumber).apply();

                                    location = obj.getString("location");
                                    prefs.edit().putString("Location", location).apply();

                                    userCountry = obj.getString("country");
                                    prefs.edit().putString("userCountry", userCountry).apply();

                                    userDisplayLanguage = obj.getString("language");
                                    prefs.edit().putString("userLanguage", userDisplayLanguage).apply();

                                    isVerified = obj.getInt("isVerified") == 1;
                                    prefs.edit().putBoolean("isVerified", isVerified).apply();

                                    getVariables(prefs);

                                    if (isVerified) {
                                        checkLocationPermission();
                                    } else {
                                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                        alertDialog.setTitle(getString(R.string.waiting_approval));
                                        alertDialog.setMessage(getString(R.string.waiting_approval_text));
                                        alertDialog.setCancelable(false);
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                });
                                        alertDialog.show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_fail), Snackbar.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("password", password);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, PERMISSIONS_LOCATION[0]) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, PERMISSIONS_LOCATION[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_PERMISSION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            loadMap();
        }
    }

    void loadMap() {
        //Detect location and set on map
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                updateMapObject();
            }
        });
    }

    private void updateMapObject() {
        if (circle != null) {
            circle.remove();
        }

        if (googleMap != null) {
            googleMap.clear();
        }

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Search stations in a radius of mapDefaultRange
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultStationRange + "&type=gas_station&key=" + getString(R.string.g_api_key);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSON json = new JSON(response);
                        if (response != null && response.length() > 0) {
                            placeID = json.key("results").index(0).key("place_id").stringValue();
                            fetchStation(placeID);
                        } else {
                            stationName = "";
                            stationVicinity = "";
                            gasolinePrice = 0;
                            dieselPrice = 0;
                            lpgPrice = 0;
                            electricityPrice = 0;
                            loadStationDetails();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(findViewById(R.id.mainContainer), "Şu anda herhangi bir istasyonda değilsiniz.", Snackbar.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    void fetchStation(final String placeID) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_FETCH_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                stationID = obj.getInt("id");
                                stationName = obj.getString("name");
                                stationVicinity = obj.getString("vicinity");
                                stationLocation = obj.getString("location");

                                //DISTANCE START
                                Location loc1 = new Location("");
                                loc1.setLatitude(Double.parseDouble(userlat));
                                loc1.setLongitude(Double.parseDouble(userlon));
                                Location loc2 = new Location("");
                                String[] stationPoint = stationLocation.split(";");
                                loc2.setLatitude(Double.parseDouble(stationPoint[0]));
                                loc2.setLongitude(Double.parseDouble(stationPoint[1]));
                                stationDistance = (int) loc1.distanceTo(loc2);
                                //DISTANCE END

                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                lpgPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");
                                lastUpdated = obj.getString("lastUpdated");
                                stationLogo = obj.getString("photoURL");

                                //Add marker
                                LatLng sydney = new LatLng(loc2.getLatitude(), loc2.getLongitude());
                                if (obj.getInt("isVerified") == 1) {
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")).icon(verifiedIcon));
                                } else {
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")));
                                }

                                //Draw a circle with radius of mapDefaultStationRange
                                circle = googleMap.addCircle(new CircleOptions()
                                        .center(new LatLng(sydney.latitude, sydney.longitude))
                                        .radius(mapDefaultStationRange)
                                        .strokeColor(Color.RED));

                                loadStationDetails();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("placeID", placeID);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void loadStationDetails() {
        stationNameHolder.setText(stationName);
        stationAddressHolder.setText(stationVicinity);

        gasolineHolder.setText("" + gasolinePrice);
        dieselHolder.setText("" + dieselPrice);
        lpgHolder.setText("" + lpgPrice);
        electricityHolder.setText("" + electricityPrice);
    }

    private void updateStation() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s != null && s.length() > 0) {
                            switch (s) {
                                case "SUCCESS":
                                    Toast.makeText(MainActivity.this, getString(R.string.stationUpdated), Toast.LENGTH_LONG).show();
                                    break;
                                case "FAIL":
                                    Toast.makeText(MainActivity.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(MainActivity.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(stationID));
                params.put("stationName", stationName);
                params.put("stationVicinity", stationVicinity);
                params.put("gasolinePrice", String.valueOf(gasolinePrice));
                params.put("dieselPrice", String.valueOf(dieselPrice));
                params.put("lpgPrice", String.valueOf(lpgPrice));
                params.put("electricityPrice", String.valueOf(electricityPrice));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void coloredBars(int color1, int color2) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color1);
            toolbar.setBackgroundColor(color2);
        } else {
            toolbar.setBackgroundColor(color2);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editProfile:
                Intent i = new Intent(MainActivity.this, ProfileEditActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are car_placeholder.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                        loadMap();
                    }
                } else {
                    Snackbar.make(this.findViewById(R.id.mainContainer), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}