package com.usama.familyca.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Interface.ApiClient;
import com.usama.familyca.Model.ModelSchedule;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewTaskActivity extends AppCompatActivity {

    private ImageView back, NewTask;
    private TextInputEditText to_be_done_text, description_text, due_date_text, due_time_text;
    private Calendar myCalendar;
    private KProgressHUD hud;
    private ModelUser modelUser;
    private String strAM_PM, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_task);

        initView();
    }

    private void initView() {

        Paper.init(AddNewTaskActivity.this);

        back = findViewById(R.id.back);
        NewTask = findViewById(R.id.NewTask);
        to_be_done_text = findViewById(R.id.to_be_done_text);
        description_text = findViewById(R.id.description_text);
        due_date_text = findViewById(R.id.due_date_text);
        due_time_text = findViewById(R.id.due_time_text);

        modelUser = Paper.book().read("currentUser");

        getTokenOfChildDeviceForScheduleUpdate();

        //Progress Bar
        hud = KProgressHUD.create(AddNewTaskActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        due_time_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddNewTaskActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (selectedHour == 0) {
                            selectedHour += 12;
                            strAM_PM = "AM";
                        } else if (selectedHour == 12) {
                            strAM_PM = "PM";
                        } else if (selectedHour > 12) {
                            selectedHour = selectedHour - 12;
                            strAM_PM = "PM";
                        } else {
                            strAM_PM = "AM";
                        }

                        due_time_text.setText(selectedHour + ":" + selectedMinute + " " + strAM_PM);
                    }
                }, hour, minute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        due_date_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddNewTaskActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        NewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }


    private String time, to_be_done, description, date;

    private void validateData() {

        date = due_date_text.getText().toString();
        to_be_done = to_be_done_text.getText().toString();
        description = description_text.getText().toString();
        time = due_time_text.getText().toString();

        if (TextUtils.isEmpty(date)) {
            due_date_text.setError("Date Required");
            return;
        }
        if (TextUtils.isEmpty(time)) {
            due_date_text.setError("Time Required");
            return;
        }
        if (TextUtils.isEmpty(to_be_done)) {
            to_be_done_text.setError("Title Required");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            description_text.setError("Description Required");
            return;
        }

        sendScheduleToDb(to_be_done, description, time, date);
    }

    private void getTokenOfChildDeviceForScheduleUpdate() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Child");
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
                        Toast.makeText(getApplicationContext(), "" + error.getMessage() + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendScheduleToDb(String to_be_done, String description, String time, String date) {
        hud.show();
        String cnic = modelUser.getE_cnic();
        String accountType = modelUser.getUid();
        String timeStamp = "" + System.currentTimeMillis();
        ModelSchedule modelSchedule = new ModelSchedule(to_be_done, description, time, cnic, date, timeStamp);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(accountType);
        reference.child(cnic).child("Schedule").child(timeStamp).setValue(modelSchedule)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hud.dismiss();
                        try {
                            prepareNotification();
                            startActivity(new Intent(AddNewTaskActivity.this, ParentHomeActivity.class));
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hud.dismiss();
                        Toast.makeText(AddNewTaskActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void prepareNotification() throws JSONException {

        JsonObject notificationJo = buildNotificationPayload();

        ApiClient.getApiService().sendNotification(notificationJo).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Notification Send Successfully", Toast.LENGTH_SHORT).show();
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
        data.addProperty("key1", "New Update!");
        data.addProperty("key2", "Your Schedule is Update: " + to_be_done);
        // add data payload
        payLoad.add("data", data);

        //compose notification here
        JsonObject notification = new JsonObject();
        notification.addProperty("title", "New Update!");
        notification.addProperty("body", "Your Schedule is Update: " + to_be_done);
        //add data payload
        payLoad.add("notification", notification);

        return payLoad;

    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        due_date_text.setText(sdf.format(myCalendar.getTime()));
    }
}