package com.usama.familyca.Interface;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiInterface {

    @Headers({
            "Content-Type: application/json",
            "Authorization: key=AAAA6tTDTNU:APA91bFeOlkAi0In_enPHxImhsq9Ps1aeVrTNCI3vi1l38Frf29M_4MVso8vn9Pg4vukaup-nKa1e3ZXflgRwhd1frqIOKS4ZUdKFko70lKK4oxyXV_tnkkze9CVtT9_bNG-boRUOq8y"
    })
    @POST("fcm/send")
    Call<JsonObject> sendNotification(@Body JsonObject payload);
}
