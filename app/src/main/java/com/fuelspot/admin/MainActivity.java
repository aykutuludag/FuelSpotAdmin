package com.fuelspot.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
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
    boolean isAtStation;
    String stationName, stationVicinity, stationCountry, stationLocation, lastUpdated, stationLogo, placeID, sonGuncelleme, istasyonSahibi;
    int stationDistance, stationID, mesafe, isStationActive, isStationVerified;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;

    CheckBox hideStation;
    EditText stationNameHolder, stationAddressHolder, gasolineHolder, dieselHolder, lpgHolder, electricityHolder;
    Button buttonUpdateStation;
    CircleImageView stationLogoHolder;
    RequestOptions options;
    BitmapDescriptor verifiedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Window
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        options = new RequestOptions().centerCrop().error(R.drawable.default_station).error(R.drawable.default_station)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);


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
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                synchronized (this) {
                    super.onLocationResult(locationResult);
                    Location locCurrent = locationResult.getLastLocation();
                    if (locCurrent != null) {
                        if (locCurrent.getAccuracy() <= mapDefaultStationRange) {
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
        stationLogoHolder = findViewById(R.id.stationLogo);
        Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);

        hideStation = findViewById(R.id.checkBox);
        hideStation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Buradaki değer sql e gidecek değer o yüzden check edilydiyse istasyonu gizle = 0;
                    isStationActive = 0;
                } else {
                    isStationActive = 1;
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

    public static String stationPhotoChooser(String stationName) {
        String photoURL;
        if (stationName.contains("Shell")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/shell.png";
        } else if (stationName.contains("Opet")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/opet.jpg";
        } else if (stationName.contains("BP")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/bp.png";
        } else if (stationName.contains("Kadoil")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kadoil.jpg";
        } else if (stationName.contains("Petrol Ofisi")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petrol-ofisi.png";
        } else if (stationName.contains("Lukoil")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/lukoil.jpg";
        } else if (stationName.contains("TP")) {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkiye-petrolleri.jpg";
        } else {
            photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/unknown.png";
        }
        return photoURL;
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
                        System.out.println("AQ: " + response);
                        JSON json = new JSON(response);
                        if (response != null && response.length() > 0) {
                            if (json.key("status").toString().equals("ZERO_RESULTS")) {
                                isAtStation = false;
                                loadStationDetails();
                            } else {
                                isAtStation = true;

                                stationName = json.key("results").index(0).key("name").stringValue();
                                stationVicinity = json.key("results").index(0).key("vicinity").stringValue();

                                double lat = json.key("results").index(0).key("geometry").key("location").key("lat").doubleValue();
                                double lon = json.key("results").index(0).key("geometry").key("location").key("lng").doubleValue();

                                Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                                try {
                                    List<Address> addresses = geo.getFromLocation(lat, lon, 1);
                                    if (addresses.size() > 0) {
                                        stationCountry = addresses.get(0).getCountryCode();
                                    } else {
                                        stationCountry = "";
                                    }
                                } catch (Exception e) {
                                    stationCountry = "";
                                }

                                stationLocation = lat + ";" + lon;

                                placeID = json.key("results").index(0).key("place_id").stringValue();

                                stationLogo = stationPhotoChooser(stationName);

                                addStation(stationName, stationVicinity, stationCountry, stationLocation, placeID, stationLogo);
                            }
                        } else {
                            isAtStation = false;
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

    private void addStation(final String sName, final String sAddress, final String sCountry, final String sLocation, final String sPlaceID, final String sLogo) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (response != null && response.length() > 0) {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                stationID = obj.getInt("id");
                                stationName = obj.getString("name");
                                stationVicinity = obj.getString("vicinity");
                                stationLocation = obj.getString("location");
                                placeID = obj.getString("googleID");
                                gasolinePrice = (float) obj.getDouble("gasolinePrice");
                                dieselPrice = (float) obj.getDouble("dieselPrice");
                                lpgPrice = (float) obj.getDouble("lpgPrice");
                                electricityPrice = (float) obj.getDouble("electricityPrice");
                                stationLogo = obj.getString("photoURL");
                                sonGuncelleme = obj.getString("lastUpdated");
                                istasyonSahibi = obj.getString("owner");
                                isStationVerified = obj.getInt("isVerified");
                                isStationActive = obj.getInt("isActive");

                                //DISTANCE START
                                Location loc = new Location("");
                                String[] stationKonum = stationLocation.split(";");
                                loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                float uzaklik = locLastKnown.distanceTo(loc);
                                mesafe = (int) uzaklik;
                                //DISTANCE END

                                //Add marker
                                LatLng sydney = new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1]));
                                if (obj.getInt("isVerified") == 1) {
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")).icon(verifiedIcon));
                                } else {
                                    googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")));
                                }

                                //Draw a circle with radius of mapDefaultStationRange
                                circle = googleMap.addCircle(new CircleOptions()
                                        .center(new LatLng(sydney.latitude, sydney.longitude))
                                        .radius(mapDefaultStationRange)
                                        .fillColor(0x220000FF)
                                        .strokeColor(Color.RED));

                                loadStationDetails();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
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
                params.put("name", sName);
                params.put("vicinity", sAddress);
                params.put("country", sCountry);
                params.put("location", sLocation);
                params.put("googleID", sPlaceID);
                params.put("photoURL", sLogo);

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void loadStationDetails() {
        if (isAtStation) {
            stationNameHolder.setText(stationName);
            stationAddressHolder.setText(stationVicinity);
            Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);

            if (isStationActive == 0) {
                hideStation.setChecked(true);
            } else {
                hideStation.setChecked(false);
            }

            gasolineHolder.setText("" + gasolinePrice);
            dieselHolder.setText("" + dieselPrice);
            lpgHolder.setText("" + lpgPrice);
            electricityHolder.setText("" + electricityPrice);
        } else {
            stationName = "";
            stationVicinity = "";
            stationLogo = "";
            gasolinePrice = 0;
            dieselPrice = 0;
            lpgPrice = 0;
            electricityPrice = 0;

            stationNameHolder.setText(stationName);
            stationAddressHolder.setText(stationVicinity);
            Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);

            gasolineHolder.setText("" + gasolinePrice);
            dieselHolder.setText("" + dieselPrice);
            lpgHolder.setText("" + lpgPrice);
            electricityHolder.setText("" + electricityPrice);
        }
    }

    private void updateStation() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s != null && s.length() > 0) {
                            switch (s) {
                                case "Success":
                                    Toast.makeText(MainActivity.this, getString(R.string.stationUpdated), Toast.LENGTH_LONG).show();
                                    break;
                                case "Fail":
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
                params.put("isActive", String.valueOf(isStationActive));

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