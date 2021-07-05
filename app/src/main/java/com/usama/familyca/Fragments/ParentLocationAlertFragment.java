package com.usama.familyca.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Activities.LoginActivity;
import com.usama.familyca.Activities.ParentHomeActivity;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import java.util.Locale;

import io.paperdb.Paper;


public class ParentLocationAlertFragment extends Fragment {

    private TextView current;
    private ModelUser modelUser;
    private MaterialButton getLoc;
    private KProgressHUD hud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent_location_alert, container, false);
        init(view);
        return view;
    }

    private void init(View view) {

        current = view.findViewById(R.id.current);
        getLoc = view.findViewById(R.id.getLoc);

        Paper.init(getContext());
        //Progress Bar
        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        getLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserCurrentLocation();
            }
        });


    }

    ModelUser mode;

    private void getUserCurrentLocation() {
        hud.show();
        modelUser = Paper.book().read("currentUser");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Child");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        hud.dismiss();
                        mode = ds.getValue(ModelUser.class);
                        if (modelUser.getE_cnic().equals(mode.getE_cnic())) {
                            current.setText(mode.getE_completeAddress());
                        }
                    }
                    String uri = "http://maps.google.com/maps?q=loc:" + mode.getLatitude() + "," + mode.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                } else {
                    hud.dismiss();
                    Toast.makeText(getContext(), "You dont have any child yet!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}