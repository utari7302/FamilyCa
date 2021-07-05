package com.usama.familyca.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Constants;
import com.usama.familyca.FetchAddressIntentServices;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

public class RegisterActivity extends AppCompatActivity {

    //implements LocationListener
    Spinner categories;
    TextView label, txt_signIn;
    String[] spinnerItem = {"Parent", "Child"};
    MaterialButton btnSignUp;
    TextInputEditText et_name, et_cnic, et_email, et_cad, et_country, et_state, et_city, et_password,
            et_repeat_password;
    ImageButton gps;

    private ResultReceiver resultReceiver;

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private double longitude = 0.0, latitude = 0.0;

    //permission constant
    public static final int LOCATION_REQUEST_CODE = 100;

    //permission arrays
    private String[] locationPermission;

    private FirebaseAuth firebaseAuth;
    private KProgressHUD hud;
    private String token;
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    private void initView() {

        resultReceiver = new AddressResultReceiver(new Handler());

        label = findViewById(R.id.label);
        categories = findViewById(R.id.categories);
        btnSignUp = findViewById(R.id.btnSignUp);

        txt_signIn = findViewById(R.id.txt_signIn);
        et_name = findViewById(R.id.et_name);
        et_cnic = findViewById(R.id.et_cnic);
        et_email = findViewById(R.id.et_email);
        et_cad = findViewById(R.id.et_cad);
        et_country = findViewById(R.id.et_country);
        et_state = findViewById(R.id.et_state);
        et_city = findViewById(R.id.et_city);
        et_password = findViewById(R.id.et_password);
        et_repeat_password = findViewById(R.id.et_repeat_password);
        gps = findViewById(R.id.gps);

        firebaseAuth = FirebaseAuth.getInstance();

        //Progress Bar
        hud = KProgressHUD.create(RegisterActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        //Spinner
        ArrayAdapter aa = new ArrayAdapter(this, R.layout.custom_spinner, spinnerItem);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(aa);
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(RegisterActivity.this, spinnerItem[position], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //initialize permission string array
        locationPermission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        //Detect Current Location
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (checkLocationPermission()) {
                    //already allowed
                    detectLocation();
                } else {
                    //not allowed request
                    requestPermission();
                }*/

                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    getCurrentLocation();
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        txt_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Log.d("TOKEN", String.valueOf(instanceIdResult.getToken()));
                token = String.valueOf(instanceIdResult.getToken());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });


        /*btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    startLocationService();
                }
            }
        });*/

    }

    private String e_name, e_cnic, e_email, e_completeAddress,
            e_country, e_state, e_city, e_spinner, e_password, e_rep_password;

    private void validateData() {

        e_name = et_name.getText().toString();
        e_cnic = et_cnic.getText().toString();
        e_email = et_email.getText().toString();
        e_completeAddress = et_cad.getText().toString();
        /*e_country = et_country.getText().toString();
        e_state = et_state.getText().toString();
        e_city = et_city.getText().toString();*/
        e_country = "";
        e_state = "";
        e_city = "";
        e_spinner = categories.getSelectedItem().toString();
        e_password = et_password.getText().toString();
        e_rep_password = et_repeat_password.getText().toString();

        if (TextUtils.isEmpty(e_name)) {
            et_name.setError("Name Required");
            return;
        }
        if (TextUtils.isEmpty(e_cnic)) {
            et_cnic.setError("CNIC Required");
            return;
        }
        if (e_cnic.length() < 13) {
            et_cnic.setError("CNIC 13 digit Required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(e_email).matches()) {
            et_email.setError("Email Required");
            return;
        }
        if (TextUtils.isEmpty(e_completeAddress)) {
            et_cad.setError("Address Required");
            return;
        }
        /*if (TextUtils.isEmpty(e_country)) {
            et_country.setError("Country Required");
            return;
        }
        if (TextUtils.isEmpty(e_state)) {
            et_state.setError("State Required");
            return;
        }
        if (TextUtils.isEmpty(e_city)) {
            et_city.setError("City Required");
            return;
        }*/
        if (TextUtils.isEmpty(e_password)) {
            et_password.setError("Password Required");
            return;
        }
        if (e_password.length() < 6) {
            et_password.setError("6 digit Password Required");
            return;
        }
        if (TextUtils.isEmpty(e_rep_password)) {
            et_repeat_password.setError("Repeat Password Required");
            return;
        }
        if (!e_password.equals(e_rep_password)) {
            et_repeat_password.setError("Repeat Password don't match");
            return;
        }

        createUserAccount();
    }

    private void createUserAccount() {

        firebaseAuth.createUserWithEmailAndPassword(e_email, e_password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (e_spinner.equals("Parent")) {
                            sendDataToDb(e_name, e_cnic, e_email, e_completeAddress,
                                    e_country, e_state, e_city, e_spinner, e_password, longitude, latitude);
                        } else {
                            checkParentExist();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkParentExist() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Parent");
        reference.orderByChild("e_cnic").equalTo(e_cnic)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            sendChildDataToDB(e_name, e_cnic, e_email, e_completeAddress,
                                    e_country, e_state, e_city, e_spinner, e_password, longitude, latitude);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Sorry! Parent is not registered yet", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    private void sendChildDataToDB(String e_name, String e_cnic, String e_email, String e_completeAddress,
                                   String e_country, String e_state, String e_city, String e_spinner, String e_password,
                                   double longitude, double latitude) {
        hud.show();
        ModelUser modelUser = new ModelUser(e_name, e_cnic, e_email, e_completeAddress, e_country,
                e_state, e_city, e_spinner, e_password, e_spinner, firebaseAuth.getUid(), longitude, latitude, true, token);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child("Child").child(e_cnic).setValue(modelUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hud.dismiss();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        hud.dismiss();
                    }
                });

    }

    private void sendDataToDb(String e_name, String e_cnic, String e_email, String e_completeAddress, String e_country,
                              String e_state, String e_city, String e_spinner, String e_password, double latitude, double longitude) {

        final String uid = "" + System.currentTimeMillis();

        hud.show();
        ModelUser modelUser = new ModelUser(e_name, e_cnic, e_email, e_completeAddress, e_country, e_state, e_city, e_spinner,
                e_password, e_spinner, firebaseAuth.getUid(), longitude, latitude, true, token);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child("Parent").child(e_cnic).setValue(modelUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hud.dismiss();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        hud.dismiss();
                    }
                });


    }

    //FUSED LOCATION WORKING
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                //permissions denied
                Toast.makeText(this, "Location Permission is necessary....", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(RegisterActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(RegisterActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            Location location = new Location("providerNA");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            fetchDataFromLatLong(location);
                        }

                    }
                }, Looper.getMainLooper());
    }

    private void fetchDataFromLatLong(Location location) {
        Intent intent = new Intent(this, FetchAddressIntentServices.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == Constants.SUCCESS_RESULT) {
                et_cad.setText(resultData.getString(Constants.RESULT_DATA_KEY));

            } else {
                Toast.makeText(RegisterActivity.this, "" + resultData.getString(Constants.RESULT_DATA_KEY), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //detect location
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        findAddress();
    }

    private void findAddress() {
        //find address,country,state,city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);//Complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set addressess
            et_country.setText(country);
            et_state.setText(state);
            et_city.setText(city);
            et_cad.setText(address);


        } catch (Exception e) {

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //gps location disabled
        Toast.makeText(this, "Please turn on location...", Toast.LENGTH_SHORT).show();
    }

    private boolean checkLocationPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted) {
                        //permissions allowed
                        detectLocation();

                    } else {
                        //permissions denied
                        Toast.makeText(this, "Location Permission is necessary....", Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //FUSED
    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction("StartLocationService");
            startService(intent);
            Toast.makeText(this, "Location Service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction("StopLocationService");
            startService(intent);
            Toast.makeText(this, "Location Service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationService();
    }

    */
}