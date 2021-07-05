package com.usama.familyca.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.usama.familyca.R;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shared = getSharedPreferences("login", MODE_PRIVATE);

        if (shared.getBoolean("logged", false)) {
            Intent intent = new Intent(getApplicationContext(), ParentHomeActivity.class);
            startActivity(intent);
            finish();
        }else{
            new Handler(Looper.getMainLooper()).postDelayed(()->{
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();

            },3000);
        }


    }
}