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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

    // Diameter of 50m circle
    public static int mapDefaultStationRange = 50;
    public static float mapDefaultZoom = 16f;

    public static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static boolean isSigned, isVerified, doubleBackToExitPressedOnce;

    public static String userPhoneNumber, userlat, userlon, name, email, password, photo, gender, birthday, location, userCountry, userCountryName, userDisplayLanguage, currencyCode, currencySymbol, username, userUnit;
    public static List<CompanyItem> companyList = new ArrayList<>();
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
    String stationName, stationVicinity, stationCountry, stationLocation, stationLogo, placeID, sonGuncelleme, istasyonSahibi, facilitiesOfStation, stationLicense;
    int stationID, mesafe, isStationActive, isStationVerified, hasMobilePayment, hasFuelDelivery;
    float gasolinePrice, dieselPrice, lpgPrice, electricityPrice;

    CheckBox hideStation, mobilOdeme, aloyakit;
    RelativeTimeTextView lastUpdateTimeText;
    EditText stationAddressHolder, gasolineHolder, dieselHolder, lpgHolder, electricityHolder, stationLicenseHolder;
    Button buttonUpdateStation;
    CircleImageView stationLogoHolder;
    RequestOptions options;
    BitmapDescriptor verifiedIcon;
    RelativeLayout verifiedLayout;
    CircleImageView imageViewWC, imageViewMarket, imageViewCarWash, imageViewTireRepair, imageViewMechanic;
    Spinner spinner;

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
        userCountry = prefs.getString("userCountry", "");
        userCountryName = prefs.getString("userCountryName", "");
        userDisplayLanguage = prefs.getString("userLanguage", "");
        userUnit = prefs.getString("userUnit", "");
        currencyCode = prefs.getString("userCurrency", "");
        userPhoneNumber = prefs.getString("userPhoneNumber", "");
        currencySymbol = prefs.getString("currencySymbol", "");
    }

    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm != null ? cm.getActiveNetworkInfo() : null) != null;
    }

    // Updated on Nov 27, 2018
    public static String stationPhotoChooser(String stationName) {
        String photoURL;
        switch (stationName) {
            case "Akçagaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/akcagaz.jpg";
                break;
            case "Akpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/akpet.jpg";
                break;
            case "Alpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/alpet.jpg";
                break;
            case "Amaco":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/amaco.jpg";
                break;
            case "Anadolugaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/anadolugaz.jpg";
                break;
            case "Antoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/antoil.jpg";
                break;
            case "Aygaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/aygaz.jpg";
                break;
            case "Aytemiz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/aytemiz.jpg";
                break;
            case "Best":
            case "Best Oil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/best.jpg";
                break;
            case "BP":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/bp.jpg";
                break;
            case "Bpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/bpet.jpg";
                break;
            case "Çekoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/cekoil.jpg";
                break;
            case "Chevron":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/chevron.jpg";
                break;
            case "Circle-K":
            case "Circle K":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/circle-k.jpg";
                break;
            case "Citgo":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/citgo.jpg";
                break;
            case "Class":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/class.jpg";
                break;
            case "Damla Petrol":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/damla-petrol.jpg";
                break;
            case "Ecogaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/ecogaz.jpg";
                break;
            case "Energy":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/energy.jpg";
                break;
            case "Erk":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/erk.jpg";
                break;
            case "Euroil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/euroil.jpg";
                break;
            case "Exxon":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/exxon.jpg";
                break;
            case "GO":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/go.jpg";
                break;
            case "Gulf":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/gulf.jpg";
                break;
            case "Güneygaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/guneygaz.jpg";
                break;
            case "Güvenal Gaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/guvenalgaz.jpg";
                break;
            case "Habaş":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/habas.jpg";
                break;
            case "İpragaz":
            case "Ipragaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/ipragaz.jpg";
                break;
            case "Jetpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/jetpet.jpg";
                break;
            case "Kadoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kadoil.jpg";
                break;
            case "Kalegaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kalegaz.jpg";
                break;
            case "Kalepet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kalepet.jpg";
                break;
            case "K-pet":
            case "Kpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/kpet.jpg";
                break;
            case "Lipetgaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/lipetgaz.jpg";
                break;
            case "Lukoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/lukoil.jpg";
                break;
            case "Marathon":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/marathon.jpg";
                break;
            case "Milangaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/milangaz.jpg";
                break;
            case "Mobil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/mobil.jpg";
                break;
            case "Mogaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/mogaz.jpg";
                break;
            case "Moil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/moil.jpg";
                break;
            case "Mola":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/mola.jpg";
                break;
            case "Opet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/opet.jpg";
                break;
            case "Pacific":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/pacific.jpg";
                break;
            case "Parkoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/parkoil.jpg";
                break;
            case "Petline":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petline.jpg";
                break;
            case "Petrol Ofisi":
            case "PO":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petrol-ofisi.jpg";
                break;
            case "Petrotürk":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/petroturk.jpg";
                break;
            case "Powerwax":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/powerwax.jpg";
                break;
            case "Qplus":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/qplus.jpg";
                break;
            case "Quicktrip":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/quicktrip.jpg";
                break;
            case "Remoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/remoil.jpg";
                break;
            case "Sanoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/sanoil.jpg";
                break;
            case "Shell":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/shell.jpg";
                break;
            case "S Oil":
            case "S-Oil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/s-oil.jpg";
                break;
            case "Starpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/starpet.jpg";
                break;
            case "Sunoco":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/sunoco.jpg";
                break;
            case "Sunpet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/sunpet.jpg";
                break;
            case "Teco":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/teco.jpg";
                break;
            case "Termo":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/termo.jpg";
                break;
            case "Texaco":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/texaco.jpg";
                break;
            case "Total":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/total.jpg";
                break;
            case "Türkiş":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkis.jpg";
                break;
            case "Türkiye Petrolleri":
            case "TP":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkiye-petrolleri.jpg";
                break;
            case "Türkoil":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkoil.jpg";
                break;
            case "Türkpetrol":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkpetrol.jpg";
                break;
            case "Turkuaz":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/turkuaz.jpg";
                break;
            case "United":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/united.jpg";
                break;
            case "Uspet":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/uspet.jpg";
                break;
            case "Valero":
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/valero.jpg";
                break;
            default:
                photoURL = "http://fuel-spot.com/FUELSPOTAPP/station_icons/unknown.jpg";
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

        spinner = findViewById(R.id.simpleSpinner);
        spinner.setOnItemSelectedListener(this);

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
        mLocationRequest.setInterval(2500);
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
        stationLicenseHolder = findViewById(R.id.editTextStationLicense);
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

        stationLogoHolder = findViewById(R.id.stationLogo);
        Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);

        hideStation = findViewById(R.id.checkBox);
        hideStation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isStationActive = 1;
                } else {
                    isStationActive = 0;
                }
            }
        });

        mobilOdeme = findViewById(R.id.checkBox2);
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

        aloyakit = findViewById(R.id.checkBox3);
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

        lastUpdateTimeText = findViewById(R.id.stationLastUpdate);

        // if stationVerified == 1, this section shows up!
        verifiedLayout = findViewById(R.id.verifiedSection);

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

        // Facilities
        imageViewWC = findViewById(R.id.WC);
        imageViewWC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtStation) {
                    if (imageViewWC.getAlpha() == 1.0f) {
                        facilitiesOfStation = facilitiesOfStation.replace("WC;", "");
                        imageViewWC.setAlpha(0.5f);
                    } else {
                        facilitiesOfStation = facilitiesOfStation + "WC;";
                        imageViewWC.setAlpha(1.0f);
                    }
                }
            }
        });

        imageViewMarket = findViewById(R.id.Market);
        imageViewMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtStation) {
                    if (imageViewMarket.getAlpha() == 1.0f) {
                        facilitiesOfStation = facilitiesOfStation.replace("Market;", "");
                        imageViewMarket.setAlpha(0.5f);
                    } else {
                        facilitiesOfStation = facilitiesOfStation + "Market;";
                        imageViewMarket.setAlpha(1.0f);
                    }
                }
            }
        });

        imageViewCarWash = findViewById(R.id.CarWash);
        imageViewCarWash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtStation) {
                    if (imageViewCarWash.getAlpha() == 1.0f) {
                        facilitiesOfStation = facilitiesOfStation.replace("CarWash;", "");
                        imageViewCarWash.setAlpha(0.5f);
                    } else {
                        facilitiesOfStation = facilitiesOfStation + "CarWash;";
                        imageViewCarWash.setAlpha(1.0f);
                    }
                }
            }
        });

        imageViewTireRepair = findViewById(R.id.TireRepair);
        imageViewTireRepair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtStation) {
                    if (imageViewTireRepair.getAlpha() == 1.0f) {
                        facilitiesOfStation = facilitiesOfStation.replace("TireRepair;", "");
                        imageViewTireRepair.setAlpha(0.5f);
                    } else {
                        facilitiesOfStation = facilitiesOfStation + "TireRepair;";
                        imageViewTireRepair.setAlpha(1.0f);
                    }
                }
            }
        });

        imageViewMechanic = findViewById(R.id.Mechanic);
        imageViewMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAtStation) {
                    if (imageViewMechanic.getAlpha() == 1.0f) {
                        facilitiesOfStation = facilitiesOfStation.replace("Mechanic;", "");
                        imageViewMechanic.setAlpha(0.5f);
                    } else {
                        facilitiesOfStation = facilitiesOfStation + "Mechanic;";
                        imageViewMechanic.setAlpha(1.0f);
                    }
                }
            }
        });

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

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    void fetchCompanies() {
        //Showing the progress dialog
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_COMPANY_FETCH),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response != null && response.length() > 0) {
                            CompanyItem item2 = new CompanyItem();
                            item2.setID(0);
                            item2.setCompanyName("Bilinmiyor");
                            item2.setCompanyLogo("http://fuel-spot.com/FUELSPOTAPP/station_icons/unknown.jpg");
                            companyList.add(item2);

                            try {
                                JSONArray res = new JSONArray(response);
                                for (int i = 0; i < res.length(); i++) {
                                    JSONObject obj = res.getJSONObject(i);

                                    CompanyItem item = new CompanyItem();
                                    item.setID(obj.getInt("id"));
                                    item.setCompanyName(obj.getString("companyName"));
                                    item.setCompanyLogo(obj.getString("companyLogo"));
                                    item.setCompanyWebsite(obj.getString("companyWebsite"));
                                    item.setCompanyPhone(obj.getString("companyPhone"));
                                    item.setCompanyAddress(obj.getString("companyAddress"));
                                    item.setNumOfStations(obj.getInt("numOfStations"));
                                    companyList.add(item);
                                }

                                CompanyAdapter customAdapter = new CompanyAdapter(MainActivity.this, companyList);
                                spinner.setAdapter(customAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
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
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
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
                            if (json.key("status").toString().equals("ZERO_RESULTS")) {
                                isAtStation = false;
                                loadStationDetails();
                            } else {
                                if (json.key("results").count() > 0) {
                                    for (int i = 0; i < json.key("results").count(); i++) {
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

                                        stationLocation = String.format(Locale.US, "%.5f", lat) + ";" + String.format(Locale.US, "%.5f", lon);

                                        placeID = json.key("results").index(0).key("place_id").stringValue();

                                        stationLogo = stationPhotoChooser(stationName);

                                        addStation(stationName, stationVicinity, stationCountry, stationLocation, placeID, stationLogo);
                                    }
                                } else {
                                    isAtStation = false;
                                    loadStationDetails();
                                }
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
                                facilitiesOfStation = obj.getString("facilities");
                                stationLogo = obj.getString("photoURL");
                                stationLicense = obj.getString("licenseNo");
                                istasyonSahibi = obj.getString("owner");
                                isStationVerified = obj.getInt("isVerified");
                                hasMobilePayment = obj.getInt("isMobilePaymentAvailable");
                                hasFuelDelivery = obj.getInt("isDeliveryAvailable");
                                isStationActive = obj.getInt("isActive");
                                sonGuncelleme = obj.getString("lastUpdated");

                                //DISTANCE START
                                Location loc = new Location("");
                                String[] stationKonum = stationLocation.split(";");
                                loc.setLatitude(Double.parseDouble(stationKonum[0]));
                                loc.setLongitude(Double.parseDouble(stationKonum[1]));
                                float uzaklik = locLastKnown.distanceTo(loc);
                                mesafe = (int) uzaklik;
                                //DISTANCE END

                                if (isStationVerified == 1) {
                                    verifiedLayout.setVisibility(View.VISIBLE);
                                } else {
                                    verifiedLayout.setVisibility(View.GONE);
                                }

                                // Facilities
                                if (facilitiesOfStation.contains("WC")) {
                                    imageViewWC.setAlpha(1.0f);
                                } else {
                                    imageViewWC.setAlpha(0.5f);
                                }

                                if (facilitiesOfStation.contains("Market")) {
                                    imageViewMarket.setAlpha(1.0f);
                                } else {
                                    imageViewMarket.setAlpha(0.5f);
                                }

                                if (facilitiesOfStation.contains("CarWash")) {
                                    imageViewCarWash.setAlpha(1.0f);
                                } else {
                                    imageViewCarWash.setAlpha(0.5f);
                                }

                                if (facilitiesOfStation.contains("TireRepair")) {
                                    imageViewTireRepair.setAlpha(1.0f);
                                } else {
                                    imageViewTireRepair.setAlpha(0.5f);
                                }

                                if (facilitiesOfStation.contains("Mechanic")) {
                                    imageViewMechanic.setAlpha(1.0f);
                                } else {
                                    imageViewMechanic.setAlpha(0.5f);
                                }

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
        if (!isAtStation) {
            stationName = "";
            stationVicinity = "";
            stationLogo = "";
            sonGuncelleme = "";
            stationLicense = "";
            isStationActive = 0;
            gasolinePrice = 0;
            dieselPrice = 0;
            lpgPrice = 0;
            electricityPrice = 0;

            // Facilities
            imageViewWC.setAlpha(0.5f);
            imageViewMarket.setAlpha(0.5f);
            imageViewCarWash.setAlpha(0.5f);
            imageViewTireRepair.setAlpha(0.5f);
            imageViewMechanic.setAlpha(0.5f);

            verifiedLayout.setVisibility(View.GONE);
            spinner.setSelection(companyList.size() - 1);
        }

        if (companyList != null && companyList.size() > 0) {
            for (int i = 0; i < companyList.size(); i++) {
                if (companyList.get(i).getCompanyName().equals(stationName)) {
                    spinner.setSelection(i, true);
                    break;
                } else {
                    // Doesn't know
                    spinner.setSelection(0);
                }
            }
        }

        stationAddressHolder.setText(stationVicinity);
        stationLicenseHolder.setText(stationLicense);
        Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);
        if (isStationActive == 0) {
            hideStation.setChecked(false);
        } else {
            hideStation.setChecked(true);
        }

        if (hasMobilePayment == 1) {
            mobilOdeme.setChecked(true);
        } else {
            mobilOdeme.setChecked(false);
        }

        if (hasFuelDelivery == 1) {
            aloyakit.setChecked(true);
        } else {
            aloyakit.setChecked(false);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(sonGuncelleme);
            lastUpdateTimeText.setReferenceTime(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            lastUpdateTimeText.setText("");
        }

        gasolineHolder.setText("" + gasolinePrice);
        dieselHolder.setText("" + dieselPrice);
        lpgHolder.setText("" + lpgPrice);
        electricityHolder.setText("" + electricityPrice);
    }

    private void updateStation() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_UPDATE_STATION),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s != null && s.length() > 0) {
                            switch (s) {
                                case "Success":
                                    Toast.makeText(MainActivity.this, getString(R.string.stationUpdated), Toast.LENGTH_LONG).show();
                                    //Re-fetch the data from server to validate and re-update last update time.
                                    updateMapObject();
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
                params.put("facilities", facilitiesOfStation);
                params.put("stationLogo", stationLogo);
                params.put("gasolinePrice", String.valueOf(gasolinePrice));
                params.put("dieselPrice", String.valueOf(dieselPrice));
                params.put("lpgPrice", String.valueOf(lpgPrice));
                params.put("electricityPrice", String.valueOf(electricityPrice));
                params.put("licenseNo", stationLicense);
                params.put("owner", istasyonSahibi);
                params.put("mobilePayment", String.valueOf(hasMobilePayment));
                params.put("fuelDelivery", String.valueOf(hasFuelDelivery));
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        stationName = companyList.get(position).getCompanyName();

        stationLogo = companyList.get(position).getCompanyLogo();
        Glide.with(this).load(stationLogo).apply(options).into(stationLogoHolder);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinner.setSelection(0);
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