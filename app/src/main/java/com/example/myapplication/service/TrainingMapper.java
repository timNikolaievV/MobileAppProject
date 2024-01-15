package com.example.myapplication.service;


import com.example.myapplication.model.Point;
import com.example.myapplication.model.Training;
import com.example.myapplication.model.TrainingWithPoints;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

public class TrainingMapper {
    public static Training trainingWithPointsToTraining(TrainingWithPoints trainingWithPoints) {
        Training training = trainingWithPoints.training;
        training.points = new Gson().toJson(trainingWithPoints.points);
        return training;
    }

    public static TrainingWithPoints trainingToTrainingWithPoints(Training training) {
        Point[] points = new Gson().fromJson(training.points, Point[].class);
        TrainingWithPoints trainingWithPoints = new TrainingWithPoints(training, Arrays.asList(points));
        return trainingWithPoints;
    }


}
