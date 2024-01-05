package com.example.myapplication.service;

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

    public void addMyPoint(Point point){
        myTraining.points.add(point);
        //TODO calculate distance, speed, time
        //TODO get opp point from ref

    }

    public void start(TrainingWithPoints myTraining, TrainingWithPoints refTraining){
        this.myTraining = myTraining;
        this.refTraining = refTraining;
        this.oppTraining = new TrainingWithPoints(refTraining.training) ;

    }

    public static TrainingWithPoints getNewRefTraining(double distance, long time){
        TrainingWithPoints ref = new TrainingWithPoints(new Training(),new ArrayList<Point>());
        //TODO generate array of points according to disctance and time

        return ref;
    }
}
