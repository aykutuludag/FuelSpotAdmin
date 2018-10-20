package com.fuelspot.admin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Currency;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.fuelspot.admin.MainActivity.birthday;
import static com.fuelspot.admin.MainActivity.currencyCode;
import static com.fuelspot.admin.MainActivity.email;
import static com.fuelspot.admin.MainActivity.gender;
import static com.fuelspot.admin.MainActivity.getVariables;
import static com.fuelspot.admin.MainActivity.isNetworkConnected;
import static com.fuelspot.admin.MainActivity.isSigned;
import static com.fuelspot.admin.MainActivity.isVerified;
import static com.fuelspot.admin.MainActivity.location;
import static com.fuelspot.admin.MainActivity.name;
import static com.fuelspot.admin.MainActivity.password;
import static com.fuelspot.admin.MainActivity.photo;
import static com.fuelspot.admin.MainActivity.userCountry;
import static com.fuelspot.admin.MainActivity.userCountryName;
import static com.fuelspot.admin.MainActivity.userDisplayLanguage;
import static com.fuelspot.admin.MainActivity.userPhoneNumber;
import static com.fuelspot.admin.MainActivity.userUnit;
import static com.fuelspot.admin.MainActivity.userlat;
import static com.fuelspot.admin.MainActivity.userlon;
import static com.fuelspot.admin.MainActivity.username;

public class LoginActivity extends AppCompatActivity {

    VideoView background;
    RelativeLayout notLogged;
    SharedPreferences prefs;
    Handler handler = new Handler();
    EditText usernameHolder, passwordHolder;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Load background and login layout
        background = findViewById(R.id.animatedBackground);
        notLogged = findViewById(R.id.notLoggedLayout);

        // Analytics
        Tracker t = ((AnalyticsApplication) this.getApplicationContext()).getDefaultTracker();
        t.setScreenName("LoginActivity");
        t.enableAdvertisingIdCollection(true);
        t.send(new HitBuilders.ScreenViewBuilder().build());

        //Variables
        prefs = this.getSharedPreferences("AdminInformation", Context.MODE_PRIVATE);
        getVariables(prefs);

        // Check whether s/he signed or not
        if (isSigned) {
            if (userlat != null && userlon != null) {
                if (userlat.length() > 0 && userlon.length() > 0) {
                    Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> addresses = geo.getFromLocation(Double.parseDouble(userlat), Double.parseDouble(userlon), 1);
                        if (addresses.size() > 0) {
                            //Check the user if s/he trip another country and if s/he is, re-logged him
                            if (!userCountry.equals(addresses.get(0).getCountryCode())) {
                                //User has changes his/her country. Fetch the tax rates/unit/currency.
                                //Language will be automatically changed
                                isSigned = false;
                                prefs.edit().putBoolean("isSigned", false).apply();

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
                        }
                    } catch (Exception e) {
                        // Do nothing
                    }
                }
            }

            notLogged.setVisibility(View.GONE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 1500);
        }

        //Layout objects
        usernameHolder = findViewById(R.id.editText);
        usernameHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    username = s.toString();
                }
            }
        });

        passwordHolder = findViewById(R.id.editText2);
        passwordHolder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    password = s.toString();
                }
            }
        });

        loginButton = findViewById(R.id.button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected(LoginActivity.this)) {
                    processTheLogin();
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void processTheLogin() {
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(LoginActivity.this, "Signing in...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_LOGIN),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        switch (response) {
                            case "Fail":
                                Snackbar.make(background, getString(R.string.login_fail), Snackbar.LENGTH_SHORT).show();
                                prefs.edit().putBoolean("isSigned", false).apply();
                                break;
                            default:
                                try {
                                    loading.dismiss();
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

                                    Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_LONG).show();
                                    notLogged.setVisibility(View.GONE);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent i = new Intent(LoginActivity.this, WelcomeActivity.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }, 1500);
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
                        //Dismissing the progress dialog
                        loading.dismiss();
                        Snackbar.make(background, getString(R.string.login_fail), Snackbar.LENGTH_SHORT).show();
                        prefs.edit().putBoolean("isSigned", false).apply();
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

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (background != null) {
            String uriPath = "android.resource://" + getPackageName() + "/" + R.raw.background_login;
            Uri uri = Uri.parse(uriPath);
            background.setVideoURI(uri);
            background.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    background.start();
                }
            });
            background.start();
        }
    }
}


