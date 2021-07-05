package com.usama.familyca.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.usama.familyca.EventBus.OnItemClick;
import com.usama.familyca.Model.ModelSchedule;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import io.paperdb.Paper;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ModelSchedule> modelScheduleArrayList;

    public ScheduleAdapter(Context context) {
        this.context = context;
        this.modelScheduleArrayList = modelScheduleArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.schedule_item_layout, parent, false);
        return new ViewHolder(view);
    }

    String type;
    String cnic, timeStamp;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Paper.init(context);

        ModelUser u = new ModelUser();
        u = Paper.book().read("currentUser");

        ModelSchedule modelSchedule = modelScheduleArrayList.get(position);

        String title = modelSchedule.getTo_be_done();
        String desc = modelSchedule.getDescription();
        String time = modelSchedule.getTime();
        String date = modelSchedule.getDate();

        timeStamp = modelSchedule.getTimeStamp();
        cnic = modelSchedule.getCnic();
        type = u.getUid();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().postSticky(new OnItemClick(true, modelScheduleArrayList.get(position).getDate(),modelScheduleArrayList.get(position).getTimeStamp()));
            }
        });

        holder.textView.setText(title);
        holder.textView4.setText(desc);
        holder.textView2.setText(date);
        holder.textView5.setText(time);

    }

    public void deleteItem(int position) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child("Parent").child(cnic).child("Schedule").child(timeStamp).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void setModelScheduleArrayList(ArrayList<ModelSchedule> modelScheduleArrayList) {
        this.modelScheduleArrayList = new ArrayList<>();
        this.modelScheduleArrayList = modelScheduleArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return modelScheduleArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView, textView2, textView4, textView5;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
            textView2 = itemView.findViewById(R.id.textView2);
            textView4 = itemView.findViewById(R.id.textView4);
            textView5 = itemView.findViewById(R.id.textView5);
        }
    }
}
