package com.example.myapplication.persistence;



import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.model.Training;

import java.util.List;

@Dao
public interface TrainingDAO {
    @Query("SELECT * FROM training")
    List<Training> getAll();

    @Query("SELECT * FROM training WHERE id_training IN (:trainingIds)")
    List<Training> loadAllByIds(int[] trainingIds);

//    @Query("SELECT * FROM training WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    User findByName(String first, String last);

    @Insert
    void insertAll(Training... trainings);

    @Delete
    void delete(Training training);
}