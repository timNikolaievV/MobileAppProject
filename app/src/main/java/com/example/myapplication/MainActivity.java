package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.model.Point;
import com.example.myapplication.model.Training;
import com.example.myapplication.model.TrainingWithPoints;
import com.example.myapplication.service.TrainingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
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

                break;

        }
    }

    public void onClickStart(View view) {
        String myTitle = "myTitle";

        if(!checkPermission()){
            requestPermission();
        }
        if (checkPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.

                            if (location != null) {
                                Point point = new Point(location.getLatitude(),
                                        location.getLongitude(),
                                        location.getAltitude(),
                                        0);

                                TrainingWithPoints myTraining = new TrainingWithPoints(myTitle, LocalDateTime.now());
                                myTraining.points.add(point);
                                EditText distanceText = (EditText) findViewById(R.id.set_distance);
                                EditText timeText = (EditText) findViewById(R.id.set_time);
                                distanceTotal = (Double.parseDouble(distanceText.getText().toString()))*1000;

                                long time = parseTimeToSeconds(timeText.getText().toString());

                                TrainingWithPoints refTraining = TrainingService.getNewRefTraining(distanceTotal, time, point);
                                trainingService.start(myTraining, refTraining);
                                running = true;
                            }
                        }
                    });
        }
        else  {
            return;//TODO no permision view
        }
    }
    private long parseTimeToSeconds(String time) {
        String [] stringArr = time.split(":");
        long sec = Long.parseLong(stringArr [stringArr.length-1]);
        long min = stringArr.length > 1 ? Long.parseLong(stringArr [stringArr.length-2]) : 0;
        long hour = stringArr.length > 2 ? Long.parseLong(stringArr [stringArr.length-3]) : 0;

        return  (hour * 3600)+(min*60)+(sec);
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,  android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
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
        //TODO add to database

        running = false;
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
                if (checkPermission()) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        Point point = new Point(location.getLatitude(),
                                                location.getLongitude(),
                                                location.getAltitude(),
                                                seconds*1000);
                                        if(mock){
                                            double deltaLatitude = trainingService.getRefDeltaLatitude();
                                            point.setLatitude(point.getLatitude()+deltaLatitude*seconds);
                                        }
                                        cordinateView.setText(point.toString());
                                        trainingService.addMyPoint(point);
                                        TrainingWithPoints myTraining = trainingService.getMyTraining();
                                        TrainingWithPoints oppTraining = trainingService.getOppTraining();
                                        primaryDisctanceView.setText(Double.toString(myTraining.training.distance));
                                        secondDisctanceView.setText(Double.toString(oppTraining.training.distance));
                                        deltaTimeView.setText(Double.toString(myTraining.training.distance-oppTraining.training.distance));

                                        primaryProgressBar.setProgress((int)((100*myTraining.training.distance)/distanceTotal));
                                        secondProgressBar.setProgress((int)((100*oppTraining.training.distance)/distanceTotal));
                                    }
                                }
                            });
                }
                //TODO stop if myTraining distance is over distanceTotal
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
}