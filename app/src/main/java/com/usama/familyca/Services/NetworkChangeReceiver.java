package com.usama.familyca.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.usama.familyca.R;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (isOnline(context)) {

            } else {
                loadErrorDialogUpdate(context);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public boolean isOnline(Context context) {

        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    AlertDialog alertDialog;

    private void loadErrorDialogUpdate(Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view;

        builder.setCancelable(false);
        view = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null);

        Button buttonAction = view.findViewById(R.id.buttonAction);

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.setCancelable(true);

        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


    }

}