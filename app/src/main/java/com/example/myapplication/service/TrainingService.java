package com.example.myapplication.service;

import android.location.Location;

import androidx.room.Room;

import com.example.myapplication.model.Point;
import com.example.myapplication.model.Training;
import com.example.myapplication.model.TrainingWithPoints;
import com.example.myapplication.persistence.TrainingDB;

import java.util.ArrayList;

public class TrainingService {
    // private TrainingDB db;
    private TrainingWithPoints myTraining;
    private TrainingWithPoints oppTraining;
    private  TrainingWithPoints refTraining;

//    public TrainingService(TrainingDB db) {
//        this.db = db;
//    }
    public TrainingWithPoints getMyTraining() {
        return myTraining;
    }
    public void setMyTraining(TrainingWithPoints myTraining) {
        this.myTraining = myTraining;
    }
    public TrainingWithPoints getOppTraining() {
        return oppTraining;
    }
    public void setOppTraining(TrainingWithPoints oppTraining) {
        this.oppTraining = oppTraining;
    }
    public TrainingWithPoints getRefTraining() {
        return refTraining;
    }
    public void setRefTraining(TrainingWithPoints refTraining) {
        this.refTraining = refTraining;
    }

    private double calcPointsDistance(Point start, Point end){
        float[] results = new float[3];
        Location.distanceBetween(start.getLatitude(),start.getLongitude(),end.getLatitude(),end.getLongitude(),results);

        return results[0];
    }

    public void addMyPoint(Point point){
        addPointToTraining(point,myTraining);
        Point oppPoint = getOppPoint(point.getTime());
        addPointToTraining(oppPoint,oppTraining);
    }

    private Point getOppPoint(long time) {
        return refTraining.points.stream().filter(x->x.getTime()>=time).findFirst().get();
        //TODO Debug
    }

    private void addPointToTraining(Point point, TrainingWithPoints training){
        if(training.points.size()!=0) {
            Point previousPoint = training.points.get(training.points.size() - 1);
            double pointsDistance = calcPointsDistance(previousPoint,point);
            training.training.distance += pointsDistance;
            training.training.time = point.getTime()*1000;
            //TODO find out how time is calculated
            //TODO calc speed using time
            training.training.speed = training.training.distance/training.training.time;

            //TODO calculate distance, speed, time

        }
        training.points.add(point);
    }

    public void start(TrainingWithPoints myTraining, TrainingWithPoints refTraining){
        this.myTraining = myTraining;
        this.refTraining = refTraining;
        this.oppTraining = new TrainingWithPoints(refTraining.training) ;
        this.oppTraining.points.add(refTraining.points.get(0));
    }

    public static TrainingWithPoints getNewRefTraining(double distance, long time, Point startPoint){
        TrainingWithPoints ref = new TrainingWithPoints(new Training(),new ArrayList<Point>());
        double speed = distance/time; //m/s
        float[] results = new float[3];
        Location.distanceBetween(startPoint.getLatitude(),startPoint.getLongitude(),startPoint.getLatitude()+0.01,startPoint.getLongitude(),results);
        double deltaLatitude = (speed*1)*(0.01/results[0]);//1 sec
        double latitude = startPoint.getLatitude();
        double longtitude = startPoint.getLongitude();
        double altitude = startPoint.getAltitude();

        for(int i = 0; i<time;i++){
            Point point = new Point(latitude, longtitude, altitude, i*1000);
            latitude+=deltaLatitude;
            ref.points.add(point);
        }
        return ref;
    }

    public double getRefDeltaLatitude(){
        //TODO check point arrays
        return refTraining.points.get(1).getLatitude()-refTraining.points.get(0).getLatitude();
    }

}
