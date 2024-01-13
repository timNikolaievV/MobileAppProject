package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.Point;
import com.example.myapplication.model.Training;
import com.example.myapplication.model.TrainingWithPoints;
import com.example.myapplication.persistence.TrainingDAO;
import com.example.myapplication.persistence.TrainingDB;
import com.example.myapplication.service.TrainingMapper;
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
    View view;
    MainViewModel viewModel;

   // private int seconds = 0;
    private double distanceTotal = 0.0;
    // Is the stopwatch running?
    private boolean running;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private FusedLocationProviderClient fusedLocationClient;
    //private TrainingService trainingService = new TrainingService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getSeconds().observe(this, seconds -> {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;

            String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

            final TextView timeView = (TextView) findViewById(R.id.time_view);
            timeView.setText(time);
        });

        viewModel.getMyTraining().observe(this, myTraining -> {
            final TextView primarySpeed = (TextView) findViewById(R.id.primary_speed);
            final TextView primaryDisctanceView = (TextView) findViewById(R.id.primary_disctance_view);
            final ProgressBar primaryProgressBar = (ProgressBar) findViewById(R.id.primay_progress_bar);

            myTraining.training.distance = Math.round(myTraining.training.distance * 100.0) / 100.0;
            myTraining.training.speed = Math.round(myTraining.training.speed * 100.0) / 100.0;

            primaryDisctanceView.setText(Double.toString(myTraining.training.distance));
            primarySpeed.setText(Double.toString(myTraining.training.speed));

            primaryProgressBar.setProgress((int) ((100 * myTraining.training.distance) / distanceTotal));
        });
        viewModel.getOppTraining().observe(this, oppTraining -> {
            final TextView secondDisctanceView = (TextView) findViewById(R.id.second_disctance_view);
            final TextView deltaTimeView = (TextView) findViewById(R.id.delta_time_view);
            final ProgressBar secondProgressBar = (ProgressBar) findViewById(R.id.second_progress_bar);

            oppTraining.training.distance = Math.round(oppTraining.training.distance * 100.0) / 100.0;
            secondDisctanceView.setText(Double.toString(oppTraining.training.distance));
            // deltaTimeView.setText(Double.toString(myTraining.training.time - oppTraining.training.time));

            secondProgressBar.setProgress((int) ((100 * oppTraining.training.distance) / distanceTotal));

        });

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
                if (locationAccepted1 && locationAccepted2) {
                    startTrainig();
                }
                break;

        }
    }

    public void onClickStart(View view) {
        List<Training> listOfTrainings = getTrainingsFromDB();
        Log.i("Info", listOfTrainings.toString());
        this.view = view;


        if (!checkPermission()) {

            requestPermission();

        } else {
            startTrainig();
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
        final boolean mock = true;
//
//        if (mock == true) {
//
//        } //Latitude changes automaticaly.


        // Creates a new Handler
        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (running && checkPermission()) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(MainActivity.this, location -> {
                                try {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        int seconds = viewModel.getSeconds().getValue();
                                        Point point = new Point(location.getLatitude(),
                                                location.getLongitude(),
                                                location.getAltitude(),
                                                seconds * 1000);
                                        if (mock) {
                                            double deltaLatitude = viewModel.getRefDeltaLatitude();
                                            point.setLatitude(point.getLatitude() + deltaLatitude * seconds);
                                        }
                                        viewModel.addMyPoint(point);
//                                            viewModel.setMyTrainingLiveData(trainingService.getMyTraining());
//                                            viewModel.setOppTrainingLiveData(trainingService.getOppTraining());

                                        if (viewModel.getMyTraining().getValue().training.distance >= distanceTotal) {
                                            stopTraining();
                                        }
                                    }
                                } catch (Exception ex) {
                                    Log.e("Error", ex.toString());
                                }
                            });
                }


                if (running) {
                    //TODO change seconds
                    int seconds = viewModel.getSeconds().getValue();
                    viewModel.setSeconds(seconds+1);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void stopTraining() {
        if (running) {
            running = false;
            saveTrainingToDB(viewModel.getMyTraining().getValue());
            Toast.makeText(getApplicationContext(), "Training is finished", Toast.LENGTH_SHORT).show();

        }
    }

    private void startTrainig() {
        String myTitle = "Running";

        if (checkPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, location -> {
                        // Got last known location. Identify location with another app before using.
                        try {
                            if (location != null) {
                                Point point = new Point(location.getLatitude(),
                                        location.getLongitude(),
                                        location.getAltitude(),
                                        0);

                                TrainingWithPoints myTraining = new TrainingWithPoints(myTitle + " " + LocalDateTime.now().toString(), LocalDateTime.now());
                                myTraining.points.add(point);

                                boolean isPacemaker = tabLayout.getSelectedTabPosition() == 1;
                                if (isPacemaker) {
                                    PacemakerFragment fragment = viewPagerAdapter.pacemakerFragment;
                                    distanceTotal = (Double.parseDouble(fragment.getDistance())) * 1000;

                                    long time = parseTimeToSeconds(fragment.getTime());

                                    TrainingWithPoints refTraining = MainViewModel.getNewRefTraining(distanceTotal, time, point);
                                    viewModel.start(myTraining, refTraining);
                                } else {

                                    //TODO get from UI
                                    TrainingFragment fragment = viewPagerAdapter.trainingFragment;
                                    Training training = fragment.getSelectedTraining();
                                    if (training == null) {
                                        Toast.makeText(getApplicationContext(), "Select training from the list", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    TrainingWithPoints refTraining = TrainingMapper.trainingToTrainingWithPoints(training);
                                    distanceTotal = training.distance;
                                    viewModel.start(myTraining, refTraining);
                                }
                                running = true;
                            }
                        } catch (Exception ex) {
                            Log.e("Error", ex.toString());
                        }
                    });
        } else {
            String stringId = "Error";
            Snackbar mySnackbar = Snackbar.make(view, stringId, 50);
            mySnackbar.show();
            return;//TODO no permision view
        }
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