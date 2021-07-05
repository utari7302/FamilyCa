package com.usama.familyca.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;
import com.usama.familyca.Activities.AddNewTaskActivity;
import com.usama.familyca.Activities.LoginActivity;
import com.usama.familyca.Activities.ParentHomeActivity;
import com.usama.familyca.Interface.ApiClient;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import org.json.JSONException;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.usama.familyca.Activities.MainActivity.shared;


public class ThrearAlertFragment extends Fragment {

    private MaterialButton button;
    private String token;
    private ModelUser modelUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_threar_alert, container, false);
        init(view);
        return view;
    }

    private void init(View view) {

        Paper.init(getContext());
        button = view.findViewById(R.id.button);
        modelUser = Paper.book().read("currentUser");

        getTokenOfParentDeviceForThreatAlert();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    prepareNotification();
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

       /* FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Log.d("TOKEN", String.valueOf(instanceIdResult.getToken()));
                token = String.valueOf(instanceIdResult.getToken());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });*/
    }


    private void getTokenOfParentDeviceForThreatAlert() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Parent");
        reference.child(modelUser.getE_cnic())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            token = "" + snapshot.child("token").getValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "" + error.getMessage() + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void prepareNotification() throws JSONException {

        JsonObject notificationJo = buildNotificationPayload();

        ApiClient.getApiService().sendNotification(notificationJo).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Notification Send Successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


    }

    private JsonObject buildNotificationPayload() {

        JsonObject payLoad = new JsonObject();
        payLoad.addProperty("to", token);

        // compose data payload here
        JsonObject data = new JsonObject();
        data.addProperty("key1", "Threat Alert");
        data.addProperty("key2", "Your child is in danger! Hurry Up. Location is: " + modelUser.getE_completeAddress());
        // add data payload
        payLoad.add("data", data);

        //compose notification here
        JsonObject notification = new JsonObject();
        notification.addProperty("title", "Threat Alert");
        notification.addProperty("body", "Your child is in danger! Hurry Up. Location is: " + modelUser.getE_completeAddress());
        //add data payload
        payLoad.add("notification", notification);

        return payLoad;

    }
}