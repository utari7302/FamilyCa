package com.usama.familyca.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.usama.familyca.Adapter.PagerAdapter;
import com.usama.familyca.Fragments.ParentLocationAlertFragment;
import com.usama.familyca.Fragments.ParentScheduleFragment;
import com.usama.familyca.Fragments.ParentSilentFeature;
import com.usama.familyca.Fragments.ScheduleFragment;
import com.usama.familyca.Fragments.ThrearAlertFragment;
import com.usama.familyca.Model.ModelUser;
import com.usama.familyca.R;

import java.util.HashMap;

import io.paperdb.Paper;

import static com.usama.familyca.Activities.MainActivity.shared;

public class ParentHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private ImageView menu, btnNotification;

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private TextView logout;
    private String token;
    private ModelUser modelUser;

    private KProgressHUD hud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        initViews();

    }

    private void initViews() {

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        menu = findViewById(R.id.menu);
        btnNotification = findViewById(R.id.btnNotification);

        Paper.init(ParentHomeActivity.this);

        modelUser = Paper.book().read("currentUser");
        logout = findViewById(R.id.logout);

        //Progress Bar
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait")
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog();
            }
        });

        viewPager = findViewById(R.id.viewpager_appointment);
        tabLayout = findViewById(R.id.tabAppointment);
        setUpViewPager(viewPager, modelUser.getUid());
        tabLayout.setupWithViewPager(viewPager);

        getSilentStateOfChildDevice();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ParentHomeActivity.this, AboutApp.class));
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Log.d("TOKEN", String.valueOf(instanceIdResult.getToken()));
                token = String.valueOf(instanceIdResult.getToken());
                if (modelUser.getUid().equals("Parent")) {
                    updateParentToken(token);
                } else {
                    updateChildToken(token);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ParentHomeActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSilentStateOfChildDevice() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Child");
        reference.child(modelUser.getE_cnic())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Boolean isSilent = (Boolean) snapshot.child("silent").getValue();
                            if (modelUser.getUid().equals("Child")) {
                                if (!(isSilent)) {
                                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                    audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                    updateSilentChildState(true);
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "" + error.getMessage() + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateParentToken(String token) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", token);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Parent");
        reference.child(modelUser.getE_cnic()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ParentHomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateChildToken(String token) {

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("token", token);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child("Child");
        reference.child(modelUser.getE_cnic()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ParentHomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setUpViewPager(ViewPager viewPager, String type) {
        if (type.equals("Parent")) {
            PagerAdapter adapter = new PagerAdapter(this.getSupportFragmentManager());
            adapter.addFragment(new ParentScheduleFragment(), "Schedule");
            adapter.addFragment(new ParentLocationAlertFragment(), "Location Alert");
            adapter.addFragment(new ParentSilentFeature(), "Silent Features");
            viewPager.setAdapter(adapter);
        } else {
            PagerAdapter adapter = new PagerAdapter(this.getSupportFragmentManager());
            adapter.addFragment(new ScheduleFragment(), "Schedule");
            adapter.addFragment(new ThrearAlertFragment(), "Threat Alert");
            viewPager.setAdapter(adapter);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_about:
                drawer.closeDrawers();
                startActivity(new Intent(ParentHomeActivity.this, AboutApp.class));
                break;
            case R.id.nav_feedback:
                drawer.closeDrawers();
                startActivity(new Intent(ParentHomeActivity.this, Feedback.class));
                break;
            case R.id.nav_help:
                drawer.closeDrawers();
                startActivity(new Intent(ParentHomeActivity.this, HelpActivity.class));
                break;
            case R.id.nav_invite:
                drawer.closeDrawers();
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "B-Learner");
                    String shareMessage = "\nLet me recommend you this application\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
                break;
            case R.id.nav_rate:
                drawer.closeDrawers();
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open", Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return true;
    }

    private void updateSilentChildState(boolean state) {

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
                        Toast.makeText(ParentHomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Confirm");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                shared.edit().putBoolean("logged", false).apply();
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                builder.setCancelable(true);
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        finish();
        finishAffinity();
    }


}