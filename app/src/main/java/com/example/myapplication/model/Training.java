package com.example.myapplication.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
@Entity
public class Training {
    @PrimaryKey(autoGenerate = true)
    public long id_training;
    @ColumnInfo(name = "title")
    public String title;
    @Ignore
    @ColumnInfo(name = "startPoint")
    public Point startPoint;
    @Ignore
    @ColumnInfo(name = "startTime")
    public LocalDateTime startTime;
    @ColumnInfo(name = "time")
    public long time;
    @ColumnInfo(name = "distance")
    public double distance;
    @ColumnInfo(name = "speed")
    public double speed;
    @ColumnInfo(name = "points")
    public String points;



}




