package com.example.myapplication.model;



import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.util.ArrayList;


@Entity
public class Training {
    @PrimaryKey(autoGenerate = true)
    int id_training;
    @ColumnInfo(name = "title")
    String title;
    @ColumnInfo(name = "startTime")
    LocalDateTime startTime;
    @ColumnInfo(name = "time")
    long time;
    @ColumnInfo(name = "distance")
    double distance;
    @ColumnInfo(name = "speed")
    double speed;


}




