package com.fuelspot.admin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.fuelspot.admin.adapter.CompanyAdapter;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public static String userPhoneNumber, userlat, userlon, name, email, password, photo, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, currencySymbol, username, userUnit;
    public static List<CompanyItem> companyList = new ArrayList<>();

    // for isNotStation
    public static int mapDefaultRange = 2500;
    public static float mapDefaultZoom = 14.5f;

    // for isAtStation
    public static int mapDefaultStationRange = 50;
    public static float mapStationZoom = 18f;
    static List<StationItem> stationList = new ArrayList<>();
    boolean mapIsUpdating;
    static ArrayList<Circle> circles = new ArrayList<>();
    static ArrayList<Marker> markers = new ArrayList<>();

    // Current station information
    boolean isAtStation;
    int stationID, isStationVerified, hasMobilePayment, hasFuelDelivery;
    String stationName, stationVicinity, stationCountry, stationLocation, stationLogo, placeID, sonGuncelleme, istasyonSahibi, facilitiesOfStation, stationLicense;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;

    // Temp variables
    int currentID;
    ArrayList<String> dummysName = new ArrayList<>();
    ArrayList<String> dummyVicinity = new ArrayList<>();
    ArrayList<String> dummyLocation = new ArrayList<>();
    ArrayList<String> dummyCountry = new ArrayList<>();
    ArrayList<String> dummyGoogleID = new ArrayList<>();
    ArrayList<String> dummyLogo = new ArrayList<>();

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

    //Layout items
    CheckBox onayliIstasyon, mobilOdeme, aloyakit;
    RelativeTimeTextView lastUpdateTimeText;
    EditText stationAddressHolder, gasolineHolder, dieselHolder, lpgHolder, electricityHolder, stationLicenseHolder;
    TextView textViewOwnerHolder, textViewStationIDHolder;
    Button buttonUpdateStation;
    CircleImageView stationLogoHolder;
    RequestOptions options;
    BitmapDescriptor verifiedIcon;
    RelativeLayout verifiedLayout;
    CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic, imageViewRestaurant, imageViewParkSpot;
    Spinner spinner;
    JSONObject facilitiesObj;

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

    // Updated on Dec 22, 2018
    public static String stationPhotoChooser(String stationName) {
        String photoURL;
        switch (stationName) {
            case "Akçagaz":
                photoURL = "https://fuel-spot.com/station_icons/akcagaz.jpg";
                break;
            case "Akpet":
                photoURL = "https://fuel-spot.com/station_icons/akpet.jpg";
                break;
            case "Alpet":
                photoURL = "https://fuel-spot.com/station_icons/alpet.jpg";
                break;
            case "Anadolugaz":
                photoURL = "https://fuel-spot.com/station_icons/anadolugaz.jpg";
                break;
            case "Antoil":
                photoURL = "https://fuel-spot.com/station_icons/antoil.jpg";
                break;
            case "Aygaz":
                photoURL = "https://fuel-spot.com/station_icons/aygaz.jpg";
                break;
            case "Aytemiz":
                photoURL = "https://fuel-spot.com/station_icons/aytemiz.jpg";
                break;
            case "Best":
                photoURL = "https://fuel-spot.com/station_icons/best.jpg";
                break;
            case "BP":
                photoURL = "https://fuel-spot.com/station_icons/bp.jpg";
                break;
            case "Bpet":
                photoURL = "https://fuel-spot.com/station_icons/bpet.jpg";
                break;
            case "Çekoil":
                photoURL = "https://fuel-spot.com/station_icons/cekoil.jpg";
                break;
            case "Class":
                photoURL = "https://fuel-spot.com/station_icons/class.jpg";
                break;
            case "Damla Petrol":
                photoURL = "https://fuel-spot.com/station_icons/damla-petrol.jpg";
                break;
            case "Ecogaz":
                photoURL = "https://fuel-spot.com/station_icons/ecogaz.jpg";
                break;
            case "Energy":
                photoURL = "https://fuel-spot.com/station_icons/energy.jpg";
                break;
            case "Erk":
                photoURL = "https://fuel-spot.com/station_icons/erk.jpg";
                break;
            case "Euroil":
                photoURL = "https://fuel-spot.com/station_icons/euroil.jpg";
                break;
            case "Exengaz":
                photoURL = "https://fuel-spot.com/station_icons/exengaz.jpg";
                break;
            case "GO":
                photoURL = "https://fuel-spot.com/station_icons/go.jpg";
                break;
            case "Gulf":
                photoURL = "https://fuel-spot.com/station_icons/gulf.jpg";
                break;
            case "Güneygaz":
                photoURL = "https://fuel-spot.com/station_icons/guneygaz.jpg";
                break;
            case "Güvenal Gaz":
                photoURL = "https://fuel-spot.com/station_icons/guvenalgaz.jpg";
                break;
            case "Habaş":
                photoURL = "https://fuel-spot.com/station_icons/habas.jpg";
                break;
            case "Hisarpet":
                photoURL = "https://fuel-spot.com/station_icons/hisarpet.jpg";
                break;
            case "İpragaz":
            case "Ipragaz":
                photoURL = "https://fuel-spot.com/station_icons/ipragaz.jpg";
                break;
            case "Jetpet":
                photoURL = "https://fuel-spot.com/station_icons/jetpet.jpg";
                break;
            case "Kadoil":
                photoURL = "https://fuel-spot.com/station_icons/kadoil.jpg";
                break;
            case "Kalegaz":
                photoURL = "https://fuel-spot.com/station_icons/kalegaz.jpg";
                break;
            case "Kalepet":
                photoURL = "https://fuel-spot.com/station_icons/kalepet.jpg";
                break;
            case "K-pet":
            case "Kpet":
                photoURL = "https://fuel-spot.com/station_icons/kpet.jpg";
                break;
            case "Ligoil":
                photoURL = "https://fuel-spot.com/station_icons/ligoil.jpg";
                break;
            case "Lipetgaz":
                photoURL = "https://fuel-spot.com/station_icons/lipetgaz.jpg";
                break;
            case "Lukoil":
                photoURL = "https://fuel-spot.com/station_icons/lukoil.jpg";
                break;
            case "Memoil":
                photoURL = "https://fuel-spot.com/station_icons/memoil.jpg";
                break;
            case "Milangaz":
                photoURL = "https://fuel-spot.com/station_icons/milangaz.jpg";
                break;
            case "Mogaz":
                photoURL = "https://fuel-spot.com/station_icons/mogaz.jpg";
                break;
            case "Moil":
                photoURL = "https://fuel-spot.com/station_icons/moil.jpg";
                break;
            case "Mola":
                photoURL = "https://fuel-spot.com/station_icons/mola.jpg";
                break;
            case "Onex":
                photoURL = "https://fuel-spot.com/station_icons/opet.jpg";
                break;
            case "Opet":
                photoURL = "https://fuel-spot.com/station_icons/opet.jpg";
                break;
            case "Pacific":
                photoURL = "https://fuel-spot.com/station_icons/pacific.jpg";
                break;
            case "Parkoil":
                photoURL = "https://fuel-spot.com/station_icons/parkoil.jpg";
                break;
            case "Petline":
                photoURL = "https://fuel-spot.com/station_icons/petline.jpg";
                break;
            case "Petrol Ofisi":
                photoURL = "https://fuel-spot.com/station_icons/petrol-ofisi.jpg";
                break;
            case "Petrotürk":
                photoURL = "https://fuel-spot.com/station_icons/petroturk.jpg";
                break;
            case "Powerwax":
                photoURL = "https://fuel-spot.com/station_icons/powerwax.jpg";
                break;
            case "Qplus":
                photoURL = "https://fuel-spot.com/station_icons/qplus.jpg";
                break;
            case "Remoil":
                photoURL = "https://fuel-spot.com/station_icons/remoil.jpg";
                break;
            case "Sanoil":
                photoURL = "https://fuel-spot.com/station_icons/sanoil.jpg";
                break;
            case "Shell":
                photoURL = "https://fuel-spot.com/station_icons/shell.jpg";
                break;
            case "S-Oil":
                photoURL = "https://fuel-spot.com/station_icons/s-oil.jpg";
                break;
            case "Starpet":
                photoURL = "https://fuel-spot.com/station_icons/starpet.jpg";
                break;
            case "Sunpet":
                photoURL = "https://fuel-spot.com/station_icons/sunpet.jpg";
                break;
            case "Teco":
                photoURL = "https://fuel-spot.com/station_icons/teco.jpg";
                break;
            case "Termo":
                photoURL = "https://fuel-spot.com/station_icons/termo.jpg";
                break;
            case "Total":
                photoURL = "https://fuel-spot.com/station_icons/total.jpg";
                break;
            case "Turkuaz":
                photoURL = "https://fuel-spot.com/station_icons/turkuaz.jpg";
                break;
            case "Türkiş":
                photoURL = "https://fuel-spot.com/station_icons/turkis.jpg";
                break;
            case "Türkiye Petrolleri":
                photoURL = "https://fuel-spot.com/station_icons/turkiye-petrolleri.jpg";
                break;
            case "Türkoil":
                photoURL = "https://fuel-spot.com/station_icons/turkoil.jpg";
                break;
            case "Türkpetrol":
                photoURL = "https://fuel-spot.com/station_icons/turkpetrol.jpg";
                break;
            case "United":
                photoURL = "https://fuel-spot.com/station_icons/united.jpg";
                break;
            case "Uspet":
                photoURL = "https://fuel-spot.com/station_icons/uspet.jpg";
                break;
            default:
                photoURL = "https://fuel-spot.com/default_icons/station.jpg";
                break;
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

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

                                if (distanceInMeter >= (mapDefaultRange / 2)) {
                                    locLastKnown.setLatitude(Double.parseDouble(userlat));
                                    locLastKnown.setLongitude(Double.parseDouble(userlon));
                                    if (!mapIsUpdating) {
                                        updateMapObject();
                                    }
                                } else {
                                    if (stationList != null && stationList.size() > 0) {
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
                                                stationName = "Bilinmiyor";
                                                stationVicinity = "";
                                                stationLocation = "";
                                                stationCountry = "";
                                                placeID = "";
                                                facilitiesOfStation = "";
                                                stationLogo = "https://fuel-spot.com/default_icons/station.jpg";
                                                gasolinePrice = 0;
                                                dieselPrice = 0;
                                                lpgPrice = 0;
                                                electricityPrice = 0;
                                                stationLicense = "";
                                                istasyonSahibi = "";
                                                isStationVerified = 0;
                                                hasMobilePayment = 0;
                                                hasFuelDelivery = 0;
                                                sonGuncelleme = "";

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
                                }
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
        };

        stationLogoHolder = findViewById(R.id.stationLogo);
        spinner = findViewById(R.id.simpleSpinner);
        stationAddressHolder = findViewById(R.id.editTextStationAddress);
        stationLicenseHolder = findViewById(R.id.editTextStationLicense);
        textViewOwnerHolder = findViewById(R.id.editTextOwner);
        textViewStationIDHolder = findViewById(R.id.textViewStationID);
        onayliIstasyon = findViewById(R.id.checkBox);
        mobilOdeme = findViewById(R.id.checkBox2);
        aloyakit = findViewById(R.id.checkBox3);
        lastUpdateTimeText = findViewById(R.id.stationLastUpdate);
        verifiedLayout = findViewById(R.id.verifiedSection);
        gasolineHolder = findViewById(R.id.editTextGasoline);
        dieselHolder = findViewById(R.id.editTextDiesel);
        lpgHolder = findViewById(R.id.editTextLPG);
        electricityHolder = findViewById(R.id.editTextElectricity);
        imageViewWC = findViewById(R.id.WC);
        imageViewMarket = findViewById(R.id.Market);
        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewRestaurant = findViewById(R.id.Restaurant);
        imageViewParkSpot = findViewById(R.id.ParkSpot);
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

        checkLocationPermission();
        fetchAccount();
        fetchCompanies();
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

        if (hasMobilePayment == 1) {
            mobilOdeme.setChecked(true);
        } else {
            mobilOdeme.setChecked(false);
        }
        mobilOdeme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hasMobilePayment = 1;
                } else {
                    hasMobilePayment = 0;
                }
            }
        });

        if (hasFuelDelivery == 1) {
            aloyakit.setChecked(true);
        } else {
            aloyakit.setChecked(false);
        }
        aloyakit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hasFuelDelivery = 1;
                } else {
                    hasFuelDelivery = 0;
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

        // Facilities
        try {
            JSONArray facilitiesRes = new JSONArray(facilitiesOfStation);
            facilitiesObj = facilitiesRes.getJSONObject(0);

            if (facilitiesObj.getInt("WC") == 1) {
                imageViewWC.setAlpha(1.0f);
            } else {
                imageViewWC.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Market") == 1) {
                imageViewMarket.setAlpha(1.0f);
            } else {
                imageViewMarket.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("CarWash") == 1) {
                imageViewCarWash.setAlpha(1.0f);
            } else {
                imageViewCarWash.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("TireRepair") == 1) {
                imageViewTireRepair.setAlpha(1.0f);
            } else {
                imageViewTireRepair.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Mechanic") == 1) {
                imageViewMechanic.setAlpha(1.0f);
            } else {
                imageViewMechanic.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("Restaurant") == 1) {
                imageViewRestaurant.setAlpha(1.0f);
            } else {
                imageViewRestaurant.setAlpha(0.5f);
            }

            if (facilitiesObj.getInt("ParkSpot") == 1) {
                imageViewParkSpot.setAlpha(1.0f);
            } else {
                imageViewParkSpot.setAlpha(0.5f);
            }
        } catch (JSONException e) {
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
                                imageViewWC.setAlpha(0.5f);
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
                                imageViewMarket.setAlpha(0.5f);
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
                                imageViewCarWash.setAlpha(0.5f);
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
                                imageViewTireRepair.setAlpha(0.5f);
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
                                imageViewMechanic.setAlpha(0.5f);
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
                                imageViewRestaurant.setAlpha(0.5f);
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
                                imageViewParkSpot.setAlpha(0.5f);
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
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                googleMap.setTrafficEnabled(true);
                if (!mapIsUpdating) {
                    updateMapObject();
                }
            }
        });
    }

    private void updateMapObject() {
        mapIsUpdating = true;
        stationList.clear();
        if (circles != null && circles.size() > 0) {
            for (int i = 0; i < circles.size(); i++) {
                circles.get(i).remove();
            }
            circles.clear();
        }

        if (markers != null && markers.size() > 0) {
            if (googleMap != null) {
                googleMap.clear();
            }
            markers.clear();
        }

        // Temp variables
        dummysName.clear();
        dummyVicinity.clear();
        dummyLocation.clear();
        dummyCountry.clear();
        dummyGoogleID.clear();
        dummyLogo.clear();

        googleMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon)))
                .radius(mapDefaultRange)
                .fillColor(0x220000FF)
                .strokeColor(Color.parseColor("#FF5635")));

        // For zooming automatically to the location of the marker
        LatLng mCurrentLocation = new LatLng(Double.parseDouble(userlat), Double.parseDouble(userlon));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCurrentLocation).zoom(mapDefaultZoom).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        searchStations("");
    }

    void searchStations(String token) {
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
                        JSON json = new JSON(response);
                        if (response != null && response.length() > 0) {
                            if (json.key("results").count() > 0) {
                                for (int i = 0; i < json.key("results").count(); i++) {
                                    dummysName.add(json.key("results").index(i).key("name").stringValue());
                                    dummyVicinity.add(json.key("results").index(i).key("vicinity").stringValue());
                                    double lat = json.key("results").index(i).key("geometry").key("location").key("lat").doubleValue();
                                    double lon = json.key("results").index(i).key("geometry").key("location").key("lng").doubleValue();
                                    dummyCountry.add(countryFinder(lat, lon));
                                    dummyLocation.add(String.format(Locale.US, "%.5f", lat) + ";" + String.format(Locale.US, "%.5f", lon));
                                    dummyGoogleID.add(json.key("results").index(i).key("place_id").stringValue());
                                    dummyLogo.add(stationPhotoChooser(json.key("results").index(i).key("name").stringValue()));
                                }

                                if (!json.key("next_page_token").isNull() && json.key("next_page_token").stringValue().length() > 0) {
                                    searchStations(json.key("next_page_token").stringValue());
                                } else {
                                    for (int i = 0; i < dummyGoogleID.size(); i++) {
                                        addStations(dummysName.get(i), dummyVicinity.get(i), dummyCountry.get(i), dummyLocation.get(i), dummyGoogleID.get(i), dummyLogo.get(i));
                                    }
                                    mapIsUpdating = true;
                                }
                            } else {
                                // Maybe s/he is in the countryside. Increase mapDefaultRange, decrease mapDefaultZoom
                                if (mapDefaultRange == 2500) {
                                    mapIsUpdating = false;

                                    mapDefaultRange = 5000;
                                    mapDefaultZoom = 12f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    if (!mapIsUpdating) {
                                        updateMapObject();
                                    }
                                } else if (mapDefaultRange == 5000) {
                                    mapIsUpdating = false;

                                    mapDefaultRange = 10000;
                                    mapDefaultZoom = 11f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    if (!mapIsUpdating) {
                                        updateMapObject();
                                    }
                                } else if (mapDefaultRange == 10000) {
                                    mapIsUpdating = false;

                                    mapDefaultRange = 25000;
                                    mapDefaultZoom = 10f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    if (!mapIsUpdating) {
                                        updateMapObject();
                                    }
                                } else if (mapDefaultRange == 25000) {
                                    mapIsUpdating = false;

                                    mapDefaultRange = 50000;
                                    mapDefaultZoom = 8.75f;
                                    Toast.makeText(MainActivity.this, "İstasyon bulunamadı. YENİ MENZİL: " + mapDefaultRange + " metre", Toast.LENGTH_SHORT).show();
                                    if (!mapIsUpdating) {
                                        updateMapObject();
                                    }
                                } else {
                                    mapIsUpdating = false;
                                    Snackbar.make(findViewById(android.R.id.content), "İstasyon bulunamadı...", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_no_location), Snackbar.LENGTH_LONG).show();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(findViewById(android.R.id.content), error.toString(), Snackbar.LENGTH_LONG).show();
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    private void addStations(final String name, final String vicinity, final String country, final String location, final String googleID, final String logo) {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_ADD_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            try {
                                JSONArray res = new JSONArray(response);
                                JSONObject obj = res.getJSONObject(0);

                                StationItem item = new StationItem();
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
                                item.setIsVerified(obj.getInt("isVerified"));
                                item.setHasSupportMobilePayment(obj.getInt("isMobilePaymentAvailable"));
                                item.setHasFuelDelivery(obj.getInt("isDeliveryAvailable"));
                                item.setLastUpdated(obj.getString("lastUpdated"));

                                //DISTANCE START
                                Location loc = new Location("");
                                String[] stationKonum = item.getLocation().split(";");
                                loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                float uzaklik = locLastKnown.distanceTo(loc);
                                item.setDistance((int) uzaklik);
                                //DISTANCE END

                                stationList.add(item);

                                // Add marker
                                LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());
                                if (obj.getInt("isVerified") == 1) {
                                    markers.add(googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")).icon(BitmapDescriptorFactory.fromResource(R.drawable.verified_station))));
                                } else {
                                    markers.add(googleMap.addMarker(new MarkerOptions().position(sydney).title(obj.getString("name")).snippet(obj.getString("vicinity")).icon(BitmapDescriptorFactory.fromResource(R.drawable.regular_station))));
                                }

                                // Draw a circle with radius of mapDefaultStationRange
                                circles.add(googleMap.addCircle(new CircleOptions()
                                        .center(new LatLng(Double.parseDouble(stationKonum[0]), Double.parseDouble(stationKonum[1])))
                                        .radius(mapDefaultStationRange)
                                        .fillColor(0x220000FF)
                                        .strokeColor(Color.parseColor("#FF5635"))));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                params.put("name", name);
                params.put("vicinity", vicinity);
                params.put("country", country);
                params.put("location", location);
                params.put("googleID", googleID);
                params.put("logoURL", logo);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void updateStation() {
        final ProgressDialog loading = ProgressDialog.show(MainActivity.this, "İstasyon güncelleniyor...", "Lütfen bekleyiniz...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            loading.dismiss();
                            switch (response) {
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
                        loading.dismiss();
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
                params.put("country", stationCountry);
                params.put("location", stationLocation);
                facilitiesOfStation = "[" + String.valueOf(facilitiesObj) + "]";
                params.put("facilities", facilitiesOfStation);
                params.put("stationLogo", stationLogo);
                params.put("gasolinePrice", String.valueOf(gasolinePrice));
                params.put("dieselPrice", String.valueOf(dieselPrice));
                params.put("lpgPrice", String.valueOf(lpgPrice));
                params.put("electricityPrice", String.valueOf(electricityPrice));
                params.put("licenseNo", stationLicense);
                params.put("owner", istasyonSahibi);
                params.put("isVerified", String.valueOf(isStationVerified));
                params.put("mobilePayment", String.valueOf(hasMobilePayment));
                params.put("fuelDelivery", String.valueOf(hasFuelDelivery));
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("password", password);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void fetchCompanies() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_COMPANY),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            CompanyItem item2 = new CompanyItem();
                            item2.setID(0);
                            item2.setName("Bilinmiyor");
                            item2.setLogo("https://fuel-spot.com/default_icons/station.jpg");
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
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
                    stationLicense = stationList.get(i).getLicenseNo();
                    istasyonSahibi = stationList.get(i).getOwner();
                    isStationVerified = stationList.get(i).getIsVerified();
                    hasMobilePayment = stationList.get(i).getHasSupportMobilePayment();
                    hasFuelDelivery = stationList.get(i).getHasFuelDelivery();
                    sonGuncelleme = stationList.get(i).getLastUpdated();
                }
                return true;
            }
        }
        return false;
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