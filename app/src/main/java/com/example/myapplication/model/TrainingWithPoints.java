package com.example.myapplication.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainingWithPoints {
    public Training training;
    public List<Point> points;

    public TrainingWithPoints(Training training, List<Point> points) {
        this.training = training;
        this.points = points;
    }

    public TrainingWithPoints(Training training) {
        this.training = training;
        this.points = new ArrayList<Point>();
    }

    public TrainingWithPoints(String title) {
        this.training = new Training();
        this.training.title = title;
        this.points = new ArrayList<Point>();
    }
}
