package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentResultListener;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.model.Point;
import com.example.myapplication.model.Training;
import com.example.myapplication.model.TrainingWithPoints;
import com.example.myapplication.persistence.TrainingDAO;
import com.example.myapplication.persistence.TrainingDB;
import com.example.myapplication.service.TrainingMapper;
import com.example.myapplication.service.TrainingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    private int seconds = 0;
    private double distanceTotal = 0.0;
    // Is the stopwatch running?
    private boolean running;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private FusedLocationProviderClient fusedLocationClient;
    private TrainingService trainingService = new TrainingService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);

        viewPagerAdapter = new ViewPagerAdapter(
                getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        // It is used to join TabLayout with ViewPager.
        tabLayout.setupWithViewPager(viewPager);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        runTimer();

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        switch (permsRequestCode) {

            case 200:

                boolean locationAccepted1 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean locationAccepted2 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                //startTrainig()
                break;

        }
    }

    public void onClickStart(View view) {
        List<Training> listOfTrainings = getTrainingsFromDB();
        Log.i("Info", listOfTrainings.toString());

        String myTitle = "Running";

        if (!checkPermission()) {

            requestPermission();

        }
//        else {
//            startTrainig();
//        }
        if (checkPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. Identify location with another app before using.
                            try {
                                if (location != null) {
                                    Point point = new Point(location.getLatitude(),
                                            location.getLongitude(),
                                            location.getAltitude(),
                                            0);

                                    TrainingWithPoints myTraining = new TrainingWithPoints(myTitle+" "+LocalDateTime.now().toString(), LocalDateTime.now());
                                    myTraining.points.add(point);

                                    boolean isPacemaker = tabLayout.getSelectedTabPosition()==1;
                                    if (isPacemaker) {
                                        PacemakerFragment fragment = viewPagerAdapter.pacemakerFragment;
                                        distanceTotal = (Double.parseDouble(fragment.getDistance())) * 1000;

                                        long time = parseTimeToSeconds(fragment.getTime());

                                        TrainingWithPoints refTraining = TrainingService.getNewRefTraining(distanceTotal, time, point);
                                        trainingService.start(myTraining, refTraining);
                                    } else {

                                        //TODO get from UI
                                        TrainingFragment fragment = viewPagerAdapter.trainingFragment;
                                        Training training = fragment.getSelectedTraining();
                                        TrainingWithPoints refTraining = TrainingMapper.trainingToTrainingWithPoints(training);
                                        trainingService.start(myTraining, refTraining);
                                    }
                                    running = true;
                                }
                            } catch (Exception ex) {
                                Log.e("Error", ex.toString());
                            }
                        }
                    });
        } else {
            String stringId = "Error";
            Snackbar mySnackbar = Snackbar.make(view, stringId, 50);
            mySnackbar.show();
            return;//TODO no permision view
        }

    }

    private long parseTimeToSeconds(String time) {
        String[] stringArr = time.split(":");
        long sec = Long.parseLong(stringArr[stringArr.length - 1]);
        long min = stringArr.length > 1 ? Long.parseLong(stringArr[stringArr.length - 2]) : 0;
        long hour = stringArr.length > 2 ? Long.parseLong(stringArr[stringArr.length - 3]) : 0;

        return (hour * 3600) + (min * 60) + (sec);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }

    private boolean checkPermission() {
//        int result = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
//        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        return ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void onClickStop(View view) {
        stopTraining();
    }

    private void runTimer() {
        //if mock = true Latitude changes autmaticly.
        final boolean mock = true;

        final TextView cordinateView = (TextView) findViewById(
                R.id.cordinate_view);
        // Get the text view.
        final TextView timeView
                = (TextView) findViewById(
                R.id.time_view);

        final TextView primaryDisctanceView = (TextView) findViewById(R.id.primary_disctance_view);
        final TextView secondDisctanceView = (TextView) findViewById(R.id.second_disctance_view);
        final TextView deltaTimeView = (TextView) findViewById(R.id.delta_time_view);

        final ProgressBar primaryProgressBar = (ProgressBar) findViewById(R.id.primay_progress_bar);
        final ProgressBar secondProgressBar = (ProgressBar) findViewById(R.id.second_progress_bar);

        // Creates a new Handler
        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (running && checkPermission()) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    try {
                                        // Got last known location. In some rare situations this can be null.
                                        if (location != null) {
                                            Point point = new Point(location.getLatitude(),
                                                    location.getLongitude(),
                                                    location.getAltitude(),
                                                    seconds * 1000);
                                            if (mock) {
                                                double deltaLatitude = trainingService.getRefDeltaLatitude();
                                                point.setLatitude(point.getLatitude() + deltaLatitude * seconds);
                                            }
                                            cordinateView.setText(point.toString());
                                            trainingService.addMyPoint(point);
                                            TrainingWithPoints myTraining = trainingService.getMyTraining();
                                            TrainingWithPoints oppTraining = trainingService.getOppTraining();
                                            //TODO format distance and speed
                                            primaryDisctanceView.setText(Double.toString(myTraining.training.distance));
                                            secondDisctanceView.setText(Double.toString(oppTraining.training.distance));
                                            deltaTimeView.setText(Double.toString(myTraining.training.distance - oppTraining.training.distance));

                                            primaryProgressBar.setProgress((int) ((100 * myTraining.training.distance) / distanceTotal));
                                            secondProgressBar.setProgress((int) ((100 * oppTraining.training.distance) / distanceTotal));

                                            if (myTraining.training.distance >= distanceTotal) {
                                                stopTraining();
                                            }
                                        }
                                    } catch (Exception ex) {
                                        Log.e("Error", ex.toString());
                                    }
                                }
                            });
                }
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                timeView.setText(time);

                if (running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void stopTraining() {
        if (running) {
            running = false;
            saveTrainingToDB(trainingService.getMyTraining());
        }
    }

    private void startTrainig() {
    }

    private void saveTrainingToDB(TrainingWithPoints trainingWithPoints) {
        try {
            TrainingDB db = Room.databaseBuilder(getApplicationContext(),
                    TrainingDB.class, "training-database").allowMainThreadQueries().build();
            TrainingDAO trainingDAO = db.trainingDAO();
            Training training = TrainingMapper.trainingWithPointsToTraining(trainingWithPoints);
            trainingDAO.insertAll(training);
        } catch (Exception ex) {
            Log.e("DBError", ex.toString());
        }
    }

    private List<Training> getTrainingsFromDB() {
        try {
            TrainingDB db = Room.databaseBuilder(getApplicationContext(),
                    TrainingDB.class, "training-database").allowMainThreadQueries().build();
            TrainingDAO trainingDAO = db.trainingDAO();

            return trainingDAO.getAll();
        } catch (Exception ex) {
            Log.e("DBError", ex.toString());
        }
        return new ArrayList<>();
    }

}