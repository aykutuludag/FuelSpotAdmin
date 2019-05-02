package com.fuelspot.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fuelspot.admin.MainActivity.PERMISSIONS_STORAGE;
import static com.fuelspot.admin.MainActivity.REQUEST_PERMISSION;
import static com.fuelspot.admin.MainActivity.birthday;
import static com.fuelspot.admin.MainActivity.email;
import static com.fuelspot.admin.MainActivity.gender;
import static com.fuelspot.admin.MainActivity.isNetworkConnected;
import static com.fuelspot.admin.MainActivity.location;
import static com.fuelspot.admin.MainActivity.name;
import static com.fuelspot.admin.MainActivity.password;
import static com.fuelspot.admin.MainActivity.photo;
import static com.fuelspot.admin.MainActivity.userCountry;
import static com.fuelspot.admin.MainActivity.userDisplayLanguage;
import static com.fuelspot.admin.MainActivity.userPhoneNumber;
import static com.fuelspot.admin.MainActivity.username;

public class ProfileEditActivity extends AppCompatActivity {

    Toolbar toolbar;
    Window window;
    CircleImageView userPic;
    EditText editName, editMail, editTextPhone, editLocation, editBirthday, editPassword;
    RadioGroup editGender;
    RadioButton bMale, bFemale, bOther;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    int calendarYear, calendarMonth, calendarDay;
    Bitmap bitmap;
    RequestQueue requestQueue;
    RequestOptions options;
    Button logOutFromAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        // Window
        window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        coloredBars(Color.parseColor("#626262"), Color.parseColor("#ffffff"));

        prefs = this.getSharedPreferences("AdminInformation", Context.MODE_PRIVATE);
        editor = prefs.edit();

        requestQueue = Volley.newRequestQueue(ProfileEditActivity.this);

        editName = findViewById(R.id.editFullName);
        editMail = findViewById(R.id.editTextMail);
        editLocation = findViewById(R.id.editTextLocation);
        editBirthday = findViewById(R.id.editTextBirthday);
        editGender = findViewById(R.id.radioGroupGender);
        bMale = findViewById(R.id.genderMale);
        bFemale = findViewById(R.id.genderFemale);
        bOther = findViewById(R.id.genderOther);

        // Setting name
        editName.setText(name);
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    name = s.toString();
                    editor.putString("Name", name);
                }
            }
        });

        // Setting email
        editMail.setText(email);
        editMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0 && s.toString().contains("@")) {
                    email = s.toString();
                    editor.putString("Email", email);
                }
            }
        });

        //UserPhoto
        userPic = findViewById(R.id.userPhoto);
        options = new RequestOptions().centerCrop().placeholder(R.drawable.default_profile).error(R.drawable.default_profile)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
        Glide.with(this).load(Uri.parse(photo)).apply(options).into(userPic);
        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.verifyFilePickerPermission(ProfileEditActivity.this)) {
                    ImagePicker.create(ProfileEditActivity.this).single().start();
                } else {
                    ActivityCompat.requestPermissions(ProfileEditActivity.this, PERMISSIONS_STORAGE, REQUEST_PERMISSION);
                }
            }
        });

        //  Setting location and retrieving changes
        editLocation.setText(location);
        editLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    location = s.toString();
                    editor.putString("Location", location);
                }
            }
        });

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPhone.setText(userPhoneNumber);
        editTextPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() > 0) {
                    userPhoneNumber = s.toString();
                    editor.putString("userPhoneNumber", userPhoneNumber);
                }
            }
        });

        //  Setting birthday and retrieving changes
        editBirthday.setText(birthday);
        if (birthday.length() > 0) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date birthDateasDate = sdf.parse(birthday);
                calendarYear = birthDateasDate.getYear() + 1900;
                calendarMonth = birthDateasDate.getMonth() + 1;
                calendarDay = birthDateasDate.getDate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            Date birthDateasDate = new Date();
            calendarYear = birthDateasDate.getYear() + 1900;
            calendarMonth = birthDateasDate.getMonth() + 1;
            calendarDay = birthDateasDate.getDate();
        }
        editBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(ProfileEditActivity.this, AlertDialog.THEME_HOLO_DARK, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        birthday = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        editBirthday.setText(birthday);
                        editor.putString("Birthday", birthday);
                    }
                }, calendarYear, calendarMonth, calendarDay);

                datePicker.setTitle("Bir tarih seçin");
                datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set", datePicker);
                datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", datePicker);
                datePicker.show();
            }
        });

        //  Set gender and retrieve changes
        switch (gender) {
            case "male":
                bMale.setChecked(true);
                break;
            case "female":
                bFemale.setChecked(true);
                break;
            default:
                bOther.setChecked(true);
                break;
        }
        editGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
                if (checkedId == R.id.genderMale) {
                    gender = "male";
                } else if (checkedId == R.id.genderFemale) {
                    gender = "female";
                } else {
                    gender = "transsexual";
                }
                editor.putString("Gender", gender);
            }
        });

        editPassword = findViewById(R.id.editTextPassword);
        editPassword.setText(password);
        editPassword.addTextChangedListener(new TextWatcher() {
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
                    editor.putString("password", password);
                }
            }
        });

        logOutFromAccount = findViewById(R.id.button5);
        logOutFromAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(ProfileEditActivity.this).create();
                alertDialog.setTitle("ÇIKIŞ YAP");
                alertDialog.setMessage("Hesaptan çıkılacak ve bu cihaza kaydedilmiş veriler silinecek?");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File sharedPreferenceFile = new File("/data/data/" + getPackageName() + "/shared_prefs/");
                                File[] listFiles = sharedPreferenceFile.listFiles();
                                for (File file : listFiles) {
                                    file.delete();
                                }

                                PackageManager packageManager = ProfileEditActivity.this.getPackageManager();
                                Intent intent = packageManager.getLaunchIntentForPackage(ProfileEditActivity.this.getPackageName());
                                ComponentName componentName = intent.getComponent();
                                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                                ProfileEditActivity.this.startActivity(mainIntent);
                                Runtime.getRuntime().exit(0);
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void updateUserInfo() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getString(R.string.API_ADMIN_UPDATE),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response) {
                            case "Success":
                                editor.apply();
                                Toast.makeText(ProfileEditActivity.this, getString(R.string.profileUpdated), Toast.LENGTH_LONG).show();
                                finish();
                                break;
                            case "Fail":
                                Toast.makeText(ProfileEditActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Showing toast
                        Toast.makeText(ProfileEditActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //Creating parameters
                Map<String, String> params = new Hashtable<>();

                //Adding parameters
                params.put("username", username);
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("phoneNumber", userPhoneNumber);
                if (bitmap != null) {
                    params.put("photo", getStringImage(bitmap));
                }
                params.put("gender", gender);
                params.put("birthday", birthday);
                params.put("location", location);
                params.put("country", userCountry);
                params.put("language", userDisplayLanguage);
                params.put("AUTH_KEY", getString(R.string.fuelspot_api_key));

                //returning parameters
                return params;
            }
        };

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.navigation_save:
                if (isNetworkConnected(this)) {
                    updateUserInfo();
                } else {
                    Toast.makeText(ProfileEditActivity.this, "İnternet bağlantısında bir sorun var", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.create(ProfileEditActivity.this).single().start();
            } else {
                Snackbar.make(findViewById(R.id.mainContainer), getString(R.string.error_permission_cancel), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Imagepicker
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Image image = ImagePicker.getFirstImageOrNull(data);
            if (image != null) {
                bitmap = BitmapFactory.decodeFile(image.getPath());
                Glide.with(this).load(bitmap).apply(options).into(userPic);
                photo = "https://fuelspot.com.tr/uploads/admin/" + username + ".jpg";
                editor.putString("ProfilePhoto", photo);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
