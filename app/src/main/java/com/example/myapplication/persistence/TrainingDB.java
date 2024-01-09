package com.example.myapplication.persistence;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.myapplication.model.Training;
@Database(entities = {Training.class}, version = 1)
public abstract class TrainingDB extends RoomDatabase {
    public abstract TrainingDAO trainingDAO();
}