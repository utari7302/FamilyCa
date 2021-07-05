package com.usama.familyca.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.usama.familyca.Model.ModelSchedule;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import java.util.ArrayList;

import io.paperdb.Paper;


public class ParentScheduleFragment extends Fragment {


    private RecyclerView parentSchedule;
    private TextView parentNothingToDo;
    private ImageView AddNewTask;

    private ArrayList<ModelSchedule> modelScheduleArrayList;
    private ScheduleAdapter scheduleAdapter;
    private ModelUser modelUser;
    private KProgressHUD hud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent_schedule, container, false);
        init(view);
        return view;
    }

    private void init(View view) {

        Paper.init(getContext());

        //Progress Bar
        hud = KProgressHUD.create(getContext())
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        parentSchedule = view.findViewById(R.id.parentSchedule);
        parentNothingToDo = view.findViewById(R.id.parentNothingToDo);
        AddNewTask = view.findViewById(R.id.AddNewTask);

        modelUser = Paper.book().read("currentUser");
        scheduleAdapter = new ScheduleAdapter(getContext());

        AddNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddNewTaskActivity.class));
            }
        });

        loadSchedulesList();
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                scheduleAdapter.deleteItem(viewHolder.getAdapterPosition());
                scheduleAdapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(parentSchedule);

    }

    private void loadSchedulesList() {

        hud.show();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(modelUser.getUid());
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
}