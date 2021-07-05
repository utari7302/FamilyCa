package com.usama.familyca.Fragments;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Activities.LoginActivity;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import java.util.HashMap;

import io.paperdb.Paper;


public class ParentSilentFeature extends Fragment {

    private Button silentButton;
    private ModelUser modelUser;
    private KProgressHUD hud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent_silent_feature, container, false);
        init(view);
        return view;
    }

    boolean state;

    private void init(View view) {
        Paper.init(getContext());
        silentButton = view.findViewById(R.id.silentButton);
        modelUser = Paper.book().read("currentUser");

        //Progress Bar
        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        silentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state = false;
                updateSilentState(state);

            }
        });
    }

    private void updateSilentState(boolean state) {

        hud.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("silent", state);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Child");
        reference.child(modelUser.getE_cnic()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hud.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


}