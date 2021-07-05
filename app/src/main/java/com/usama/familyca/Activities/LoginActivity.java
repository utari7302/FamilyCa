package com.usama.familyca.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;
import com.usama.familyca.Services.NetworkChangeReceiver;

import io.paperdb.Paper;

import static com.usama.familyca.Activities.MainActivity.shared;

public class LoginActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    Spinner categories;
    String[] spinnerItem = {"Parent", "Child"};
    private KProgressHUD hud;
    private TextInputEditText et_email, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Paper.init(LoginActivity.this);
        categories = findViewById(R.id.categories);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);

        broadcastReceiver = new NetworkChangeReceiver();

        registerNetworkBroadCastReceiver();

        Paper.init(getApplicationContext());

        findViewById(R.id.txt_forget).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
        findViewById(R.id.txt_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
        findViewById(R.id.btnSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                validateData();
            }
        });

        //Progress Bar
        hud = KProgressHUD.create(LoginActivity.this)
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

    }

    private String e_email, e_password, e_spinner;

    private void validateData() {

        e_email = et_email.getText().toString();
        e_password = et_password.getText().toString();
        e_spinner = categories.getSelectedItem().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(e_email).matches()) {
            et_email.setError("Email Required");
            return;
        }

        if (TextUtils.isEmpty(e_password)) {
            et_password.setError("Password Required");
            return;
        }
        if (e_password.length() < 6) {
            et_password.setError("6 digit Password Required");
            return;
        }

        checkUser();

    }

    private void checkUser() {
        if (e_spinner.equals("Parent")) {
            loginParentAsUser();
        } else {
            loginChildAsUser();
        }
    }

    private boolean state;

    private void loginParentAsUser() {
        hud.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(e_spinner);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        ModelUser userModel = ds.getValue(ModelUser.class);
                        if (userModel.getE_email().equals(e_email) && userModel.getE_password().equals(e_password)) {
                            Paper.book().write("currentUser", userModel);
                            state = true;
                        }
                    }
                    if (state) {
                        hud.dismiss();
                        Intent intent = new Intent(LoginActivity.this, ParentHomeActivity.class);
                        shared.edit().putBoolean("logged", true).apply();
                        startActivity(intent);
                        finish();
                    } else {
                        hud.dismiss();
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hud.dismiss();
                Toast.makeText(LoginActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginChildAsUser() {
        hud.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(e_spinner);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        ModelUser userModel = ds.getValue(ModelUser.class);
                        if (userModel.getE_email().equals(e_email) && userModel.getE_password().equals(e_password)) {
                            Paper.book().write("currentUser", userModel);
                            state = true;
                        }

                    }
                    if (state) {
                        hud.dismiss();
                        Intent intent = new Intent(LoginActivity.this, ParentHomeActivity.class);
                        shared.edit().putBoolean("logged", true).apply();
                        startActivity(intent);
                        finish();

                    } else {
                        hud.dismiss();
                        Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hud.dismiss();
                Toast.makeText(LoginActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    protected void registerNetworkBroadCastReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    protected void unRegisterNetwork() {
        try {
//            unregisterReceiver(broadcastReceiver);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
 //       unRegisterNetwork();
    }
}