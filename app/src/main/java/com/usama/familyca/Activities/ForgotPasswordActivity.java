package com.usama.familyca.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import java.util.HashMap;

public class ForgotPasswordActivity extends AppCompatActivity {

    Spinner categories;
    String[] spinnerItem = {"Parent", "Child"};
    MaterialButton btnResetPassword;
    TextInputEditText et_cnic, et_password, et_repeat_password;
    private FirebaseAuth firebaseAuth;
    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        categories = findViewById(R.id.categories);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        et_cnic = findViewById(R.id.et_cnic);
        et_password = findViewById(R.id.et_password);
        et_repeat_password = findViewById(R.id.et_repeat_password);

        firebaseAuth = FirebaseAuth.getInstance();

        //Progress Bar
        hud = KProgressHUD.create(ForgotPasswordActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

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

    private String e_cnic, e_spinner, e_password, e_rep_password;

    private void validateData() {


        e_cnic = et_cnic.getText().toString();
        e_spinner = categories.getSelectedItem().toString();
        e_password = et_password.getText().toString();
        e_rep_password = et_repeat_password.getText().toString();


        if (TextUtils.isEmpty(e_cnic)) {
            et_cnic.setError("CNIC Required");
            return;
        }
        if (e_cnic.length() < 13) {
            et_cnic.setError("CNIC 13 digit Required");
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
        if (TextUtils.isEmpty(e_rep_password)) {
            et_repeat_password.setError("Repeat Password Required");
            return;
        }
        if (!e_password.equals(e_rep_password)) {
            et_repeat_password.setError("Repeat Password don't match");
            return;
        }

        checkParentExist();
    }


    private void checkParentExist() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Parent");
        reference.orderByChild("e_cnic").equalTo(e_cnic)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() != null) {
                            updatePassword(e_password);
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Sorry! User with thic cnic is not exist yet", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void updatePassword(String e_password) {
        hud.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("e_password", e_password);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(e_spinner);
        reference.child(e_cnic).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hud.dismiss();
                        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        Toast.makeText(ForgotPasswordActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}


