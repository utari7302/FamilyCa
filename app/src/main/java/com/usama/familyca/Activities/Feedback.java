package com.usama.familyca.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.R;

import java.util.HashMap;

public class Feedback extends AppCompatActivity {

    private ImageView back;
    private EditText subject, message;
    private MaterialButton btnSendTicket;
    private String sub, mes;
    private FirebaseDatabase firebaseDatabase;
    private KProgressHUD hud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        back = findViewById(R.id.back);
        subject = findViewById(R.id.subject);
        message = findViewById(R.id.message);
        btnSendTicket = findViewById(R.id.btnSendTicket);
        firebaseDatabase = FirebaseDatabase.getInstance();


        btnSendTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        //Progress Bar
        hud = KProgressHUD.create(Feedback.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void validateData() {
        sub = subject.getText().toString();
        mes = message.getText().toString();

        if (TextUtils.isEmpty(sub)) {
            subject.setError("Subject Required");
            return;
        }
        if (TextUtils.isEmpty(mes)) {
            message.setError("Description Required");
            return;
        }

        requestForHelp(sub, mes);
    }

    private void requestForHelp(String sub, String mes) {

        hud.show();
        final String timestamp = "" + System.currentTimeMillis();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("subject", sub);
        hashMap.put("message", mes);

        DatabaseReference reference = firebaseDatabase.getReference("FeedBack");
        reference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hud.dismiss();
                        clearText();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hud.dismiss();
                Toast.makeText(Feedback.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void clearText() {

        subject.setText("");
        message.setText("");

    }
}