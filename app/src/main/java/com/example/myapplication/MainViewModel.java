package com.example.myapplication;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myapplication.model.Point;
import com.example.myapplication.model.Training;
import com.example.myapplication.model.TrainingWithPoints;

import java.util.ArrayList;

public class MainViewModel extends ViewModel {
    private TrainingWithPoints refTraining;

    private final MutableLiveData<Integer> secondsLiveData =
            new MutableLiveData(0);
    private final MutableLiveData<TrainingWithPoints> myTrainingLiveData =
            new MutableLiveData(new TrainingWithPoints(new Training()));
    private final MutableLiveData<TrainingWithPoints> oppTrainingLiveData =
            new MutableLiveData(new TrainingWithPoints(new Training()));

    public LiveData<Integer> getSeconds() {

        return secondsLiveData;
    }

    public LiveData<TrainingWithPoints> getMyTraining() {

        return myTrainingLiveData;
    }
    public LiveData<TrainingWithPoints> getOppTraining() {

        return oppTrainingLiveData;
    }

    public void setSeconds(int seconds) {
        secondsLiveData.postValue(seconds);
    }

    public void setMyTrainingLiveData(TrainingWithPoints myTraining){
        myTrainingLiveData.postValue(myTraining);
    }
    public void setOppTrainingLiveData(TrainingWithPoints oppTraining){
        oppTrainingLiveData.postValue(oppTraining);
    }
    public void addMyPoint(Point point) {
        myTrainingLiveData.postValue(addPointToTraining(point, myTrainingLiveData.getValue()));
        Point oppPoint = getOppPoint(point.getTime());
        oppTrainingLiveData.postValue(addPointToTraining(oppPoint, oppTrainingLiveData.getValue()));
    }
    public void start(TrainingWithPoints myTraining, TrainingWithPoints refTraining) {
        myTrainingLiveData.postValue(myTraining);
        this.refTraining = refTraining;
        TrainingWithPoints oppTraining = new TrainingWithPoints(refTraining.training);
        oppTraining.training.distance = 0.0;
        oppTraining.training.time = 0;
        oppTraining.points.add(refTraining.points.get(0));
        oppTrainingLiveData.postValue(oppTraining);
    }
    public static TrainingWithPoints getNewRefTraining(double distance, long time, Point startPoint) {
        TrainingWithPoints ref = new TrainingWithPoints(new Training(), new ArrayList<Point>());
        double speed = distance / time; //m/s
        float[] results = new float[3];
        Location.distanceBetween(startPoint.getLatitude(), startPoint.getLongitude(), startPoint.getLatitude() + 0.01, startPoint.getLongitude(), results);
        double deltaLatitude = (speed * 1) * (0.01 / results[0]);//1 sec
        double latitude = startPoint.getLatitude();
        double longtitude = startPoint.getLongitude();
        double altitude = startPoint.getAltitude();

        for (int i = 0; i <= time; i++) {
            Point point = new Point(latitude, longtitude, altitude, i * 1000);
            latitude += deltaLatitude;
            ref.points.add(point);
        }
        return ref;
    }

    public double getRefDeltaLatitude() {
        //TODO check point arrays
        return refTraining.points.get(1).getLatitude() - refTraining.points.get(0).getLatitude();
    }


    private Point getOppPoint(long time) {
        return refTraining.points.stream().filter(x -> x.getTime() >= time).findFirst().get();
        //TODO Debug
    }

    private TrainingWithPoints addPointToTraining(Point point, TrainingWithPoints training) {
        if (training.points.size() != 0) {
            Point previousPoint = training.points.get(training.points.size() - 1);
            double pointsDistance = calcPointsDistance(previousPoint, point);
            training.training.distance += pointsDistance;
            training.training.time = point.getTime();
            //TODO find out how time is calculated
            //TODO calc speed using time
            training.training.speed = training.training.distance / training.training.time;

            //TODO calculate distance, speed, time

        }
        training.points.add(point);
        return training;
    }

    private double calcPointsDistance(Point start, Point end) {
        float[] results = new float[3];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude(), results);

        return Math.round(results[0] * 1000.0) / 1000.0;
    }
}
