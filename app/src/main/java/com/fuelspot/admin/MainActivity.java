package com.fuelspot.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.fuelspot.admin.adapter.CompanyAdapter;
import com.fuelspot.admin.adapter.MarkerAdapter;
import com.fuelspot.admin.model.CompanyItem;
import com.fuelspot.admin.model.StationItem;
import com.github.curioustechizen.ago.RelativeTimeTextView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import eu.amirs.JSON;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final int REQUEST_PERMISSION = 0;
    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static boolean isSigned;
    public static boolean isVerified;
    public static boolean doubleBackToExitPressedOnce;
    // Admin variables
    public static String token, userPhoneNumber, userlat, userlon, name, email, password, photo, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, currencySymbol, username, userUnit;
    public static List<CompanyItem> companyList = new ArrayList<>();

    // for isNotStation
    public static int mapDefaultRange = 3000;
    public static float mapDefaultZoom = 12.75f;

    // for isAtStation
    public static int mapDefaultStationRange = 50;
    public static float mapStationZoom = 18f;
    static List<StationItem> stationList = new ArrayList<>();
    List<StationItem> googleStations = new ArrayList<>();
    boolean mapIsUpdating;
    static ArrayList<Circle> circles = new ArrayList<>();
    static ArrayList<Marker> markers = new ArrayList<>();

    int stationID, isStationVerified;
    String stationName, stationVicinity, stationCountry, stationLocation, stationLogo, placeID, sonGuncelleme, istasyonSahibi, facilitiesOfStation, stationLicense, secondaryFuelOfStation;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice, gasoline2, diesel2;

    // Temp variables
    int currentID;

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

    // For adding station over places-api
    static List<StationItem> oldStationList = new ArrayList<>();
    CheckBox onayliIstasyon;
    RelativeTimeTextView lastUpdateTimeText;
    EditText stationAddressHolder, gasolineHolder, dieselHolder, lpgHolder, electricityHolder, stationLicenseHolder, gasolineHolder2, dieselHolder2;
    TextView textViewOwnerHolder, textViewStationIDHolder;
    Button buttonUpdateStation;
    CircleImageView stationLogoHolder;
    RequestOptions options;
    BitmapDescriptor verifiedIcon;
    RelativeLayout verifiedLayout;
    CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant, imageViewParkSpot, imageViewATM, imageViewMotel, imageViewCoffeeShop, imageViewMosque;
    Spinner spinner;
    JSONObject facilitiesObj = new JSONObject();
    JSONObject secondaryFuelObj = new JSONObject();
    List<String> googleIDs = new ArrayList<>();

    //Layout items
    Button buttonMissingStation;
    ProgressDialog dialog;
    ScrollView mScrollView;
    // Current station information
    boolean isAtStation, doesOldStationsShown;

    public static void getVariables(SharedPreferences prefs) {
        name = prefs.getString("Name", "");
        email = prefs.getString("Email", "");
        password = prefs.getString("password", "");
        photo = prefs.getString("ProfilePhoto", "");
        gender = prefs.getString("Gender", "");
        birthday = prefs.getString("Birthday", "");
        location = prefs.getString("Location", "");
        username = prefs.getString("UserName", "");
        userlat = prefs.getString("lat", "39.925054");
        userlon = prefs.getString("lon", "32.8347552");
        isSigned = prefs.getBoolean("isSigned", false);
        isVerified = prefs.getBoolean("isVerified", false);
        userCountry = prefs.getString("userCountry", "");
        userCountryName = prefs.getString("userCountryName", "");
        userDisplayLanguage = prefs.getString("userLanguage", "");
        userUnit = prefs.getString("userUnit", "");
        currencyCode = prefs.getString("userCurrency", "");
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
        currencySymbol = prefs.getString("currencySymbol", "");
        token = prefs.getString("token", "");
    }

    public static boolean verifyFilePickerPermission(Context context) {
        boolean hasStorage = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                hasStorage = true;
            }
        } else {
            hasStorage = true;
        }
        return hasStorage;
    }

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    public static String stationPhotoChooser(String stationName) {
        String photoURL = "https://fuelspot.com.tr/default_icons/station.png";
        for (int i = 0; i < companyList.size(); i++) {
            if (stationName.equals(companyList.get(i).getName())) {
                photoURL = companyList.get(i).getLogo();
                break;
            }
        }
        return photoURL;
    }

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
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        ;

        // ProgressDialogs
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("Bölge taranıyor");
        dialog.setMessage("Bu işlem 1-2 dakika sürebilir. Lütfen bekleyiniz...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        // Activate map
        MapsInitializer.initialize(this.getApplicationContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        verifiedIcon = BitmapDescriptorFactory.fromResource(R.drawable.verified_station);

        mScrollView = findViewById(R.id.scrollView);
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

                                float distanceInMeter = locLastKnown.distanceTo(locCurrent);

                                if (stationList != null) {
                                    if (stationList.size() == 0 || distanceInMeter >= (mapDefaultRange / 2f)) {
                                        locLastKnown.setLatitude(Double.parseDouble(userlat));
                                        locLastKnown.setLongitude(Double.parseDouble(userlon));
                                        if (!mapIsUpdating) {
                                            updateMapObject();
                                        }
                                    } else {
                                        for (int i = 0; i < stationList.size(); i++) {
                                            String[] stationLocation = stationList.get(i).getLocation().split(";");
                                            double stationLat = Double.parseDouble(stationLocation[0]);
                                            double stationLon = Double.parseDouble(stationLocation[1]);

                                            Location locStation = new Location("");
                                            locStation.setLatitude(stationLat);
                                            locStation.setLongitude(stationLon);

                                            float newDistance = locCurrent.distanceTo(locStation);
                                            stationList.get(i).setDistance((int) newDistance);
                                        }

                                        isAtStation = isWorkerAtStation();

                                        if (isAtStation) {
                                            if (stationID != currentID) {
                                                loadLayoutItems();

                                                // For zooming automatically to the location of the marker
                                                if (googleMap != null) {
                                                    LatLng mCurrentLocation = new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1]));
                                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapStationZoom).build();
                                                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                }
                                            }
                                        } else {
                                            if (stationID != 0) {
                                                stationID = 0;
                                                stationName = "";
                                                stationVicinity = "";
                                                stationLocation = "";
                                                stationCountry = "";
                                                placeID = "";
                                                facilitiesOfStation = "";
                                                stationLogo = "https://fuelspot.com.tr/default_icons/station.png";
                                                gasolinePrice = 0;
                                                dieselPrice = 0;
                                                lpgPrice = 0;
                                                electricityPrice = 0;
                                                gasoline2 = 0;
                                                diesel2 = 0;
                                                stationLicense = "";
                                                secondaryFuelOfStation = "";
                                                istasyonSahibi = "";
                                                isStationVerified = 0;
                                                sonGuncelleme = "";
                                                spinner.setSelection(0);

                                                loadLayoutItems();

                                                // For zooming automatically to the location of the marker
                                                if (googleMap != null) {
                                                    LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
                                                    CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
                                                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (!mapIsUpdating) {
                                        updateMapObject();
                                    }
                                }
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        };

        buttonMissingStation = findViewById(R.id.buttonEksikIstasyon);
        buttonMissingStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchStationsOverGoogle("");
            }
        });
        stationLogoHolder = findViewById(R.id.stationLogo);
        spinner = findViewById(R.id.simpleSpinner);
        stationAddressHolder = findViewById(R.id.editTextStationAddress);
        stationLicenseHolder = findViewById(R.id.editTextStationLicense);
        textViewOwnerHolder = findViewById(R.id.editTextOwner);
        textViewStationIDHolder = findViewById(R.id.textViewStationID);
        onayliIstasyon = findViewById(R.id.checkBox);
        lastUpdateTimeText = findViewById(R.id.stationLastUpdate);
        verifiedLayout = findViewById(R.id.verifiedSection);
        gasolineHolder = findViewById(R.id.editTextGasoline);
        dieselHolder = findViewById(R.id.editTextDiesel);
        lpgHolder = findViewById(R.id.editTextLPG);
        electricityHolder = findViewById(R.id.editTextElectricity);
        gasolineHolder2 = findViewById(R.id.editTextGasoline2);
        dieselHolder2 = findViewById(R.id.editTextDiesel2);
        imageViewWC = findViewById(R.id.WC);
        imageViewMarket = findViewById(R.id.Market);
        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewRestaurant = findViewById(R.id.Restaurant);
        imageViewParkSpot = findViewById(R.id.ParkSpot);
        imageViewATM = findViewById(R.id.ATM);
        imageViewMotel = findViewById(R.id.Motel);
        imageViewCoffeeShop = findViewById(R.id.CoffeeShop);
        imageViewMosque = findViewById(R.id.PlaceOfWorship);
        buttonUpdateStation = findViewById(R.id.buttonUpdate);
        buttonUpdateStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtStation) {
                    updateStation();
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.uAreNotAtStation), Snackbar.LENGTH_LONG).show();
                }
            }
        });

        if (isNetworkConnected(this)) {
            checkLocationPermission();
            fetchAccount();
            fetchCompanies();
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        }
    }

    void loadLayoutItems() {
        spinner.setOnItemSelectedListener(MainActivity.this);
        if (companyList != null && companyList.size() > 0) {
            for (int i = 0; i < companyList.size(); i++) {
                if (companyList.get(i).getName().equals(stationName)) {
                    spinner.setSelection(i, true);
                    break;
                }
            }
        }

        String dummyId = "" + stationID;
        textViewStationIDHolder.setText(dummyId);

        // Layout items
        stationAddressHolder.setText(stationVicinity);
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

        stationLicenseHolder.setText(stationLicense);
        stationLicenseHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    stationLicense = s.toString();
                }
            }
        });

        if (istasyonSahibi != null && istasyonSahibi.length() > 0) {
            textViewOwnerHolder.setText(istasyonSahibi);
        } else {
            textViewOwnerHolder.setText(getString(R.string.noOwner));
        }


        Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);


        // if stationVerified == 1, this section shows up!
        if (isStationVerified == 1) {
            onayliIstasyon.setChecked(true);
            verifiedLayout.setVisibility(View.VISIBLE);
        } else {
            onayliIstasyon.setChecked(false);
            verifiedLayout.setVisibility(View.GONE);
        }
        onayliIstasyon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isStationVerified = 1;
                    onayliIstasyon.setChecked(true);
                    verifiedLayout.setVisibility(View.VISIBLE);
                } else {
                    isStationVerified = 0;
                    onayliIstasyon.setChecked(false);
                    verifiedLayout.setVisibility(View.GONE);
                }
            }
        });

        if (sonGuncelleme != null && sonGuncelleme.length() > 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                Date date = format.parse(sonGuncelleme);
                lastUpdateTimeText.setReferenceTime(date.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                lastUpdateTimeText.setText("");
            }
        }

        String dummyG = "" + gasolinePrice;
        gasolineHolder.setText(dummyG);
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

        String dummyD = "" + dieselPrice;
        dieselHolder.setText(dummyD);
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

        String dummyL = "" + lpgPrice;
        lpgHolder.setText(dummyL);
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

        String dummyE = "" + electricityPrice;
        electricityHolder.setText(dummyE);
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

        // SecondaryFuel
        try {
            JSONArray secondaryFuelRes = new JSONArray(secondaryFuelOfStation);
            secondaryFuelObj = secondaryFuelRes.getJSONObject(0);

            if (secondaryFuelObj.has("gasoline2") && secondaryFuelObj.getString("gasoline2").length() > 0) {
                gasoline2 = Float.parseFloat(secondaryFuelObj.getString("gasoline2"));
                gasolineHolder2.setText("" + gasoline2);
            } else {
                gasoline2 = 0;
            }

            if (secondaryFuelObj.has("diesel2") && secondaryFuelObj.getString("diesel2").length() > 0) {
                diesel2 = Float.parseFloat(secondaryFuelObj.getString("diesel2"));
                dieselHolder2.setText("" + diesel2);
            } else {
                diesel2 = 0;
            }
        } catch (JSONException e) {
            gasoline2 = 0;
            diesel2 = 0;
            e.printStackTrace();
        }

        String dummyB2 = "" + gasoline2;
        gasolineHolder2.setText(dummyB2);
        gasolineHolder2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    try {
                        gasoline2 = Float.parseFloat(s.toString());
                        String text = String.format(Locale.US, "%.2f", gasoline2);
                        secondaryFuelObj.put("gasoline2", text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        String dummyBD2 = "" + diesel2;
        dieselHolder2.setText(dummyBD2);
        dieselHolder2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    try {
                        diesel2 = Float.parseFloat(s.toString());
                        String text = String.format(Locale.US, "%.2f", diesel2);
                        secondaryFuelObj.put("diesel2", text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Facilities
        try {
            JSONArray facilitiesRes = new JSONArray(facilitiesOfStation);
            facilitiesObj = facilitiesRes.getJSONObject(0);

            if (facilitiesObj.getInt("WC") == 1) {
                imageViewWC.setAlpha(1.0f);
            } else {
                imageViewWC.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Market") == 1) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("CarWash") == 1) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("TireRepair") == 1) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Mechanic") == 1) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Restaurant") == 1) {
                imageViewRestaurant.setAlpha(1.0f);
            } else {
                imageViewRestaurant.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("ParkSpot") == 1) {
                imageViewParkSpot.setAlpha(1.0f);
            } else {
                imageViewParkSpot.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("ATM") == 1) {
                imageViewATM.setAlpha(1.0f);
            } else {
                imageViewATM.setAlpha(0.25f);
            }

            if (facilitiesObj.getInt("Motel") == 1) {
                imageViewMotel.setAlpha(1.0f);
            } else {
                imageViewMotel.setAlpha(0.25f);
            }

            /* NEW FACILITIES v1.1 */
            if (!facilitiesObj.has("CoffeeShop")) {
                facilitiesObj.put("CoffeeShop", "0");
                imageViewCoffeeShop.setAlpha(0.25f);
            } else {
                if (facilitiesObj.getInt("CoffeeShop") == 1) {
                    imageViewCoffeeShop.setAlpha(1.0f);
                } else {
                    imageViewCoffeeShop.setAlpha(0.25f);
                }
            }

            if (!facilitiesObj.has("Mosque")) {
                facilitiesObj.put("Mosque", "0");
                imageViewMosque.setAlpha(0.25f);
            } else {
                if (facilitiesObj.getInt("Mosque") == 1) {
                    imageViewMosque.setAlpha(1.0f);
                } else {
                    imageViewMosque.setAlpha(0.25f);
                }
            }
            /* NEW FACILITIES v1.1 */
        } catch (JSONException e) {
            imageViewWC.setAlpha(0.25f);
            imageViewMarket.setAlpha(0.25f);
            imageViewCarWash.setAlpha(0.25f);
            imageViewTireRepair.setAlpha(0.25f);
            imageViewMechanic.setAlpha(0.25f);
            imageViewRestaurant.setAlpha(0.25f);
            imageViewParkSpot.setAlpha(0.25f);
            imageViewATM.setAlpha(0.25f);
            imageViewMotel.setAlpha(0.25f);
            imageViewCoffeeShop.setAlpha(0.25f);
            imageViewMosque.setAlpha(0.25f);
            e.printStackTrace();
        }

        if (facilitiesObj != null) {
            imageViewWC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("WC") == 1) {
                                facilitiesObj.put("WC", "0");
                                imageViewWC.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("WC", "1");
                                imageViewWC.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewMarket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("Market") == 1) {
                                facilitiesObj.put("Market", "0");
                                imageViewMarket.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("Market", "1");
                                imageViewMarket.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewCarWash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("CarWash") == 1) {
                                facilitiesObj.put("CarWash", "0");
                                imageViewCarWash.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("CarWash", "1");
                                imageViewCarWash.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewTireRepair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("TireRepair") == 1) {
                                facilitiesObj.put("TireRepair", "0");
                                imageViewTireRepair.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("TireRepair", "1");
                                imageViewTireRepair.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewMechanic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("Mechanic") == 1) {
                                facilitiesObj.put("Mechanic", "0");
                                imageViewMechanic.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("Mechanic", "1");
                                imageViewMechanic.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewRestaurant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("Restaurant") == 1) {
                                facilitiesObj.put("Restaurant", "0");
                                imageViewRestaurant.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("Restaurant", "1");
                                imageViewRestaurant.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewParkSpot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("ParkSpot") == 1) {
                                facilitiesObj.put("ParkSpot", "0");
                                imageViewParkSpot.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("ParkSpot", "1");
                                imageViewParkSpot.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewATM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("ATM") == 1) {
                                facilitiesObj.put("ATM", "0");
                                imageViewATM.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("ATM", "1");
                                imageViewATM.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewMotel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("Motel") == 1) {
                                facilitiesObj.put("Motel", "0");
                                imageViewMotel.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("Motel", "1");
                                imageViewMotel.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewCoffeeShop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("CoffeeShop") == 1) {
                                facilitiesObj.put("CoffeeShop", "0");
                                imageViewCoffeeShop.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("CoffeeShop", "1");
                                imageViewCoffeeShop.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            imageViewMosque.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isAtStation) {
                        try {
                            if (facilitiesObj.getInt("Mosque") == 1) {
                                facilitiesObj.put("Mosque", "0");
                                imageViewMosque.setAlpha(0.25f);
                            } else {
                                facilitiesObj.put("Mosque", "1");
                                imageViewMosque.setAlpha(1.0f);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, PERMISSIONS_LOCATION[0]) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, PERMISSIONS_LOCATION[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSIONS_LOCATION[0], PERMISSIONS_LOCATION[1]}, REQUEST_PERMISSION);
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            loadMap();
        }
    }

    @SuppressLint("MissingPermission")
    void loadMap() {
        //Detect location and set on map
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.setTrafficEnabled(true);
                googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            mScrollView.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                });

                // For zooming automatically to the location of the marker
                LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                MarkerAdapter customInfoWindow = new MarkerAdapter(MainActivity.this);
                googleMap.setInfoWindowAdapter(customInfoWindow);
            }
        });
    }

    private void updateMapObject() {
        if (googleMap != null) {
            mapIsUpdating = true;
            stationList.clear();
            circles.clear();
            markers.clear();
            googleMap.clear();

            //Draw a circle with radius of mapDefaultRange
            googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                    .radius(mapDefaultRange)
                    .fillColor(0x220000FF)
                    .strokeColor(Color.parseColor("#FF5635")));

            // For zooming automatically to the location of the marker
            CameraPosition cameraPosition;
            if (isAtStation) {
                LatLng mCurrentLocation = new LatLng(Double.parseDouble(stationLocation.split(";")[0]), Double.parseDouble(stationLocation.split(";")[1]));
                cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapStationZoom).build();
            } else {
                LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
                cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
            }

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            if (isNetworkConnected(this)) {
                fetchStations();
            } else {
                mapIsUpdating = false;
                Toast.makeText(MainActivity.this, "İnternet bağlantısında bir sorun var", Toast.LENGTH_LONG).show();
            }
        } else {
            mapIsUpdating = false;
            Toast.makeText(MainActivity.this, "İnternet bağlantısında bir sorun var", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchStations() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.API_ADMIN_SEARCH_STATION) + "?location=" + userlat + ";" + userlon + "&radius=" + mapDefaultRange,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);

                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);
                                    final StationItem item = new StationItem();
                                    item.setID(obj.getInt("id"));
                                    item.setStationName(obj.getString("name"));
                                    item.setVicinity(obj.getString("vicinity"));
                                    item.setCountryCode(obj.getString("country"));
                                    item.setLocation(obj.getString("location"));
                                    item.setGoogleMapID(obj.getString("googleID"));
                                    item.setFacilities(obj.getString("facilities"));
                                    item.setLicenseNo(obj.getString("licenseNo"));
                                    item.setOwner(obj.getString("owner"));
                                    item.setPhotoURL(obj.getString("logoURL"));
                                    item.setGasolinePrice((float) obj.getDouble("gasolinePrice"));
                                    item.setDieselPrice((float) obj.getDouble("dieselPrice"));
                                    item.setLpgPrice((float) obj.getDouble("lpgPrice"));
                                    item.setElectricityPrice((float) obj.getDouble("electricityPrice"));
                                    item.setSecondaryFuels(obj.getString("otherFuels"));
                                    item.setIsVerified(obj.getInt("isVerified"));
                                    item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                                    item.setHasFuelDelivery(obj.getInt("isDeliveryAvailable"));
                                    item.setLastUpdated(obj.getString("lastUpdated"));
                                    item.setIsActive(obj.getInt("isActive"));
                                    item.setLastUpdated(obj.getString("lastUpdated"));
                                    item.setDistance((int) obj.getDouble("distance"));
                                    Glide.with(MainActivity.this)
                                            .asBitmap().load(item.getPhotoURL())
                                            .listener(new RequestListener<Bitmap>() {
                                                          @Override
                                                          public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                                              return false;
                                                          }

                                                          @Override
                                                          public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                                              item.setStationLogoDrawable(new BitmapDrawable(MainActivity.this.getResources(), bitmap));
                                                              return false;
                                                          }
                                                      }
                                            ).submit();
                                    stationList.add(item);
                                    googleIDs.add(item.getGoogleMapID());
                                }

                                mapIsUpdating = false;

                                // We are waiting for loading logos
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        addMarkers();
                                    }
                                }, 500);
                            } catch (JSONException e) {
                                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            reTry();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_SHORT).show();
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void addMarkers() {
        markers.clear();
        googleMap.clear();

        if (doesOldStationsShown) {
            for (int i = 0; i < oldStationList.size(); i++) {
                StationItem sItem = oldStationList.get(i);
                String[] stationKonum = sItem.getLocation().split(";");
                LatLng sydney = new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1]));

                Location ankara = new Location("");
                ankara.setLatitude(sydney.latitude);
                ankara.setLongitude(sydney.longitude);
                boolean isDuplicated = checkForDuplicate(ankara, i);

                if (isDuplicated) {
                    if (sItem.getIsVerified() == 1) {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    } else {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.duplicate_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    }

                    circles.add(googleMap.addCircle(new CircleOptions()
                            .center(sydney)
                            .radius(mapDefaultStationRange)
                            .fillColor(0x22000000)
                            .strokeColor(Color.parseColor("#000000"))));
                } else {
                    if (sItem.getIsVerified() == 1) {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    } else {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.regular_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    }

                    circles.add(googleMap.addCircle(new CircleOptions()
                            .center(sydney)
                            .radius(mapDefaultStationRange)
                            .fillColor(0x220000FF)
                            .strokeColor(Color.parseColor("#FF5635"))));
                }
            }
        } else {
            for (int i = 0; i < stationList.size(); i++) {
                StationItem sItem = stationList.get(i);
                String[] stationKonum = sItem.getLocation().split(";");
                LatLng sydney = new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1]));

                Location ankara = new Location("");
                ankara.setLatitude(sydney.latitude);
                ankara.setLongitude(sydney.longitude);
                boolean isDuplicated = checkForDuplicate(ankara, i);

                if (isDuplicated) {
                    if (sItem.getIsVerified() == 1) {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    } else {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.duplicate_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    }

                    circles.add(googleMap.addCircle(new CircleOptions()
                            .center(sydney)
                            .radius(mapDefaultStationRange)
                            .fillColor(0x22000000)
                            .strokeColor(Color.parseColor("#000000"))));
                } else {
                    if (sItem.getIsVerified() == 1) {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    } else {
                        MarkerOptions mOptions = new MarkerOptions().position(sydney).title(sItem.getStationName()).snippet(sItem.getVicinity()).icon(BitmapDescriptorFactory.fromResource(R.drawable.regular_station));
                        Marker m = googleMap.addMarker(mOptions);
                        m.setTag(sItem);
                        markers.add(m);
                    }

                    circles.add(googleMap.addCircle(new CircleOptions()
                            .center(sydney)
                            .radius(mapDefaultStationRange)
                            .fillColor(0x220000FF)
                            .strokeColor(Color.parseColor("#FF5635"))));
                }
            }
        }

        //Draw a circle with radius of mapDefaultRange
        googleMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                .radius(mapDefaultRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));
    }

    private boolean checkForDuplicate(Location ank, int index) {
        for (int j = 0; j < stationList.size(); j++) {
            Location locStation = new Location("");
            locStation.setLatitude(Double.parseDouble(stationList.get(j).getLocation().split(";")[0]));
            locStation.setLongitude(Double.parseDouble(stationList.get(j).getLocation().split(";")[1]));
            if (locStation.distanceTo(ank) <= 50f && index != j) {
                return true;
            }
        }
        return false;
    }

    private void updateStation() {
        final ProgressDialog loading = ProgressDialog.show(MainActivity.this, "İstasyon güncelleniyor...", "Lütfen bekleyiniz...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        if (response != null && response.length() > 0) {
                            if (response.equals("Success")) {
                                Toast.makeText(MainActivity.this, getString(R.string.stationUpdated), Toast.LENGTH_LONG).show();
                                updateMapObject();
                            } else {
                                Toast.makeText(MainActivity.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        loading.dismiss();
                        Toast.makeText(MainActivity.this, getString(R.string.stationUpdateFail), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("stationID", String.valueOf(stationID));
                params.put("stationName", stationName);
                params.put("stationVicinity", stationVicinity);
                params.put("country", stationCountry);
                params.put("location", stationLocation);
                facilitiesOfStation = "[" + facilitiesObj + "]";
                params.put("facilities", facilitiesOfStation);
                params.put("stationLogo", stationLogo);
                params.put("gasolinePrice", String.valueOf(gasolinePrice));
                params.put("dieselPrice", String.valueOf(dieselPrice));
                params.put("lpgPrice", String.valueOf(lpgPrice));
                params.put("electricityPrice", String.valueOf(electricityPrice));
                secondaryFuelOfStation = "[" + secondaryFuelObj + "]";
                params.put("otherFuels", secondaryFuelOfStation);
                params.put("licenseNo", stationLicense);
                params.put("owner", istasyonSahibi);
                params.put("isVerified", String.valueOf(isStationVerified));
                params.put("isActive", String.valueOf(1));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void fetchAccount() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_LOGIN),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "Success":
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

                                    if (!isVerified) {
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
                            case "Fail":
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.login_fail), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                                finish();
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
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }

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

    void fetchCompanies() {
        if (companyList == null || companyList.size() == 0) {
            //Showing the progress dialog
            StringRequest stringRequest = new StringRequest(Request.Method.GET, getString(R.string.OTHER_COMPANY),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null && response.length() > 0) {
                                if (response.equals("AuthError")) {
                                    //We're just checking here for any authentication error. If it is, log out.

                                    // Do logout
                                    @SuppressLint("SdCardPath")
                                    File sharedPreferenceFile = new File("/data/data/" + getPackageName() + "/shared_prefs/");
                                    File[] listFiles = sharedPreferenceFile.listFiles();
                                    for (File file : listFiles) {
                                        file.delete();
                                    }

                                    PackageManager packageManager = MainActivity.this.getPackageManager();
                                    Intent intent = packageManager.getLaunchIntentForPackage(MainActivity.this.getPackageName());
                                    ComponentName componentName = intent.getComponent();
                                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                                    MainActivity.this.startActivity(mainIntent);
                                    Runtime.getRuntime().exit(0);
                                }

                                CompanyItem item2 = new CompanyItem();
                                item2.setID(0);
                                item2.setName("Bilinmiyor");
                                item2.setLogo("https://fuelspot.com.tr/default_icons/station.png");
                                companyList.add(item2);
                                try {
                                    JSONArray res = new JSONArray(response);
                                    for (int i = 0; i < res.length(); i++) {
                                        JSONObject obj = res.getJSONObject(i);

                                        CompanyItem item = new CompanyItem();
                                        item.setID(obj.getInt("id"));
                                        item.setName(obj.getString("companyName"));
                                        item.setLogo(obj.getString("companyLogo"));
                                        item.setWebsite(obj.getString("companyWebsite"));
                                        item.setPhone(obj.getString("companyPhone"));
                                        item.setAddress(obj.getString("companyAddress"));
                                        item.setNumOfVerifieds(obj.getInt("numOfVerifieds"));
                                        item.setNumOfStations(obj.getInt("numOfStations"));
                                        companyList.add(item);
                                    }

                                    CompanyAdapter customAdapter = new CompanyAdapter(MainActivity.this, companyList);
                                    spinner.setAdapter(customAdapter);
                                } catch (JSONException e) {
                                    Snackbar.make(findViewById(android.R.id.content), e.toString(), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.connection_error), Snackbar.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            //Adding request to the queue
            requestQueue.add(stringRequest);
        }
    }

    boolean isWorkerAtStation() {
        if (textViewStationIDHolder.getText() != null && textViewStationIDHolder.getText().length() > 0) {
            currentID = Integer.parseInt(textViewStationIDHolder.getText().toString());
        } else {
            currentID = 0;
        }

        for (int i = 0; i < stationList.size(); i++) {
            if (stationList.get(i).getDistance() <= 50) {
                if (stationList.get(i).getID() == 0 || stationList.get(i).getID() != currentID) {
                    stationID = stationList.get(i).getID();
                    stationName = stationList.get(i).getStationName();
                    stationVicinity = stationList.get(i).getVicinity();
                    stationLocation = stationList.get(i).getLocation();
                    stationCountry = stationList.get(i).getCountryCode();
                    placeID = stationList.get(i).getGoogleMapID();
                    facilitiesOfStation = stationList.get(i).getFacilities();
                    stationLogo = stationList.get(i).getPhotoURL();
                    gasolinePrice = stationList.get(i).getGasolinePrice();
                    dieselPrice = stationList.get(i).getDieselPrice();
                    lpgPrice = stationList.get(i).getLpgPrice();
                    electricityPrice = stationList.get(i).getElectricityPrice();
                    secondaryFuelOfStation = stationList.get(i).getSecondaryFuels();
                    stationLicense = stationList.get(i).getLicenseNo();
                    istasyonSahibi = stationList.get(i).getOwner();
                    isStationVerified = stationList.get(i).getIsVerified();
                    sonGuncelleme = stationList.get(i).getLastUpdated();
                }
                return true;
            }
        }
        return false;
    }

    void reTry() {
        // Maybe s/he is in the countryside. Increase mapDefaultRange, decrease mapDefaultZoom
        if (mapDefaultRange == 3000) {
            mapIsUpdating = false;

            mapDefaultRange = 6000;
            mapDefaultZoom = 12f;
            Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            if (!mapIsUpdating) {
                updateMapObject();
            }
        } else if (mapDefaultRange == 6000) {
            mapIsUpdating = false;

            mapDefaultRange = 10000;
            mapDefaultZoom = 11.5f;
            Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            if (!mapIsUpdating) {
                updateMapObject();
            }
        } else if (mapDefaultRange == 10000) {
            mapIsUpdating = false;

            mapDefaultRange = 25000;
            mapDefaultZoom = 11f;
            Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            if (!mapIsUpdating) {
                updateMapObject();
            }
        } else if (mapDefaultRange == 25000) {
            mapIsUpdating = false;

            mapDefaultRange = 50000;
            mapDefaultZoom = 10f;
            Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
            if (!mapIsUpdating) {
                updateMapObject();
            }
        } else {
            mapIsUpdating = false;
            Snackbar.make(findViewById(android.R.id.content), "İstasyon bulunamadı...", Snackbar.LENGTH_LONG).show();
        }
    }

    void searchStationsOverGoogle(String token) {
        dialog.show();
        String url;

        if (token != null && token.length() > 0) {
            // For getting next 20 stations
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultRange + "&type=gas_station&pagetoken=" + token + "&key=" + getString(R.string.g_api_key);
        } else {
            // For getting first 20 stations
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + userlat + "," + userlon + "&radius=" + mapDefaultRange + "&type=gas_station" + "&key=" + getString(R.string.g_api_key);
        }

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            JSON json = new JSON(response);
                            JSONObject obj = json.getJsonObject();

                            if (json.key("results").count() > 0) {
                                for (int i = 0; i < json.key("results").count(); i++) {
                                    if (!googleIDs.contains(json.key("results").index(i).key("place_id").stringValue())) {
                                        StationItem sItem = new StationItem();
                                        sItem.setGoogleMapID(json.key("results").index(i).key("place_id").stringValue());
                                        sItem.setStationName(json.key("results").index(i).key("name").stringValue());
                                        sItem.setVicinity(json.key("results").index(i).key("vicinity").stringValue());
                                        double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                        double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();

                                        sItem.setLocation(String.format(Locale.US, "%.5f", lat) + ";" + String.format(Locale.US, "%.5f", lon));
                                        sItem.setPhotoURL(stationPhotoChooser(json.key("results").index(i).key("name").stringValue()));
                                        sItem.setCountryCode(countryFinder(lat, lon));
                                        googleStations.add(sItem);
                                    }
                                }

                                if (!obj.has("next_page_token") || obj.optString("next_page_token").length() == 0) {
                                    addStations();
                                } else {
                                    searchStationsOverGoogle(json.key("next_page_token").stringValue());
                                }
                            } else {
                                // Maybe s/he is in the countryside. Increase mapDefaultRange, decrease mapDefaultZoom
                                if (mapDefaultRange == 3000) {
                                    dialog.dismiss();
                                    mapDefaultRange = 6000;
                                    mapDefaultZoom = 12.75f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    searchStationsOverGoogle("");
                                } else if (mapDefaultRange == 5000) {
                                    dialog.dismiss();
                                    mapDefaultRange = 10000;
                                    mapDefaultZoom = 11f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    searchStationsOverGoogle("");
                                } else if (mapDefaultRange == 10000) {
                                    dialog.dismiss();
                                    mapDefaultRange = 25000;
                                    mapDefaultZoom = 9.5f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    searchStationsOverGoogle("");
                                } else if (mapDefaultRange == 20000) {
                                    dialog.dismiss();
                                    mapDefaultRange = 50000;
                                    mapDefaultZoom = 8.75f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    searchStationsOverGoogle("");
                                } else {
                                    dialog.dismiss();
                                    Snackbar.make(findViewById(android.R.id.content), "İstasyon bulunamadı...", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            dialog.dismiss();
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }

                    private String countryFinder(double lat, double lon) {
                        Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                        try {
                            List<Address> addresses = geo.getFromLocation(lat, lon, 1);
                            if (addresses.size() > 0) {
                                return addresses.get(0).getCountryCode();
                            } else {
                                return "";
                            }
                        } catch (Exception e) {
                            return "";
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), error.toString(), Snackbar.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    /* This method add_fuel stations. If station exists in db, then update it (except prices). Returns stationInfos.
     * To update stationPrices, use API_UPDATE_STATION */
    private void addStations() {
        for (int i = 0; i < googleStations.size(); i++) {
            //Showing the progress dialog
            final int finalI = i;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_ADD_STATION),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response + " " + finalI);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            volleyError.printStackTrace();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    //Creating parameters
                    Map<String, String> params = new Hashtable<>();

                    params.put("name", googleStations.get(finalI).getStationName());
                    params.put("vicinity", googleStations.get(finalI).getVicinity());
                    params.put("country", googleStations.get(finalI).getCountryCode());
                    params.put("location", googleStations.get(finalI).getLocation());
                    params.put("googleID", googleStations.get(finalI).getGoogleMapID());
                    params.put("logoURL", googleStations.get(finalI).getPhotoURL());

                    //returning parameters
                    return params;
                }
            };

            //Adding request to the queue
            requestQueue.add(stringRequest);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(1000);

            googleStations.clear();
            dialog.dismiss();
            Toast.makeText(MainActivity.this, "Bölge başarıyla tarandı.", Toast.LENGTH_SHORT).show();

            updateMapObject();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        stationName = companyList.get(position).getName();
        stationLogo = companyList.get(position).getLogo();
        Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinner.setSelection(0, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_showOldStations):
                if (!doesOldStationsShown) {
                    oldStationList.clear();
                    for (int i = 0; i < stationList.size(); i++) {
                        StationItem tempItem = stationList.get(i);
                        if (tempItem.getGasolinePrice() == 0f && tempItem.getDieselPrice() == 0f && tempItem.getLpgPrice() == 0f) {
                            oldStationList.add(tempItem);
                        }
                    }
                    doesOldStationsShown = true;
                    Toast.makeText(MainActivity.this, "Fiyatı olmayan istasyonlar gösterliyor.", Toast.LENGTH_LONG).show();
                } else {
                    doesOldStationsShown = false;
                    Toast.makeText(MainActivity.this, "Bütün istasyonlar gösteriliyor.", Toast.LENGTH_LONG).show();
                }

                addMarkers();
                break;
            case (R.id.action_editProfile):
                Intent i = new Intent(MainActivity.this, ProfileEditActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                    loadMap();
                }
            } else {
                Snackbar.make(this.findViewById(R.id.mainContainer), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
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
            mapIsUpdating = false;
            stationList.clear();
            googleStations.clear();
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