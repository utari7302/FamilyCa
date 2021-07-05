package com.usama.familyca.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Activities.AddNewTaskActivity;
import com.usama.familyca.Adapter.ScheduleAdapter;
import com.usama.familyca.EventBus.OnItemClick;
import com.usama.familyca.Model.ModelSchedule;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.paperdb.Paper;

public class ScheduleFragment extends Fragment {

    private RecyclerView parentSchedule;
    private TextView parentNothingToDo;

    private ArrayList<ModelSchedule> modelScheduleArrayList;
    private ScheduleAdapter scheduleAdapter;
    private ModelUser modelUser;
    private KProgressHUD hud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        init(view);
        return view;
    }

    private void init(View view) {

        Paper.init(getContext());

        parentSchedule = view.findViewById(R.id.parentSchedule);
        parentNothingToDo = view.findViewById(R.id.parentNothingToDo);


        modelUser = Paper.book().read("currentUser");
        scheduleAdapter = new ScheduleAdapter(getContext());

        //Progress Bar
        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        loadSchedulesList();

    }


    private void loadSchedulesList() {
        hud.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Parent");
        reference.child(modelUser.getE_cnic()).child("Schedule")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hud.dismiss();
                        modelScheduleArrayList = new ArrayList<>();
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelSchedule modelSchedule = ds.getValue(ModelSchedule.class);
                                modelScheduleArrayList.add(modelSchedule);
                            }
                            scheduleAdapter.setModelScheduleArrayList(modelScheduleArrayList);
                            parentSchedule.setAdapter(scheduleAdapter);


                        }
                        if (modelScheduleArrayList.isEmpty()) {
                            parentSchedule.setVisibility(View.GONE);
                            parentNothingToDo.setVisibility(View.VISIBLE);
                        } else {
                            parentSchedule.setVisibility(View.VISIBLE);
                            parentNothingToDo.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hud.dismiss();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    String d, t;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnItemClick event) {
        if (event.isClick()) {

            event.setClick(false);
            d = event.getDate();
            t = event.getTime();
            long dateInMillis = getMilliFromDate(d);

            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int date = cal.get(Calendar.DATE);
            cal.clear();
            cal.set(year, month, date);
            long todayMillis2 = cal.getTimeInMillis();

            if (todayMillis2 > dateInMillis) {
                loadErrorDialogUpdate(t);
            } else {
                Toast.makeText(getContext(), "This event can't be deleted. Before its occurring time", Toast.LENGTH_SHORT).show();
            }


        }


    }

    public void deleteSchedule(String t) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("Parent").child(modelUser.getE_cnic()).child("Schedule").child(t).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot : dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    AlertDialog alertDialog;

    private void loadErrorDialogUpdate(String t) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view;

        builder.setCancelable(false);
        view = LayoutInflater.from(getContext()).inflate(R.layout.custom_dialog, null);

        Button buttonAction = view.findViewById(R.id.buttonAction);
        ImageView imageIcon = view.findViewById(R.id.imageIcon);
        TextView statusT = view.findViewById(R.id.statusT);
        TextView descriptionT = view.findViewById(R.id.descriptionT);

        imageIcon.setImageResource(R.drawable.reject);
        statusT.setText("Hopefully done with schedule!");
        descriptionT.setText("Do you really want to delete this schedule from the list ?");
        buttonAction.setText("Yes, Sure");

        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.setCancelable(true);

        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSchedule(t);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


    }

    public long getMilliFromDate(String dateFormat) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
        try {
            date = formatter.parse(dateFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date.getTime();
    }
}