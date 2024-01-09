package com.example.myapplication;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.example.myapplication.model.Training;
import com.example.myapplication.persistence.TrainingDAO;
import com.example.myapplication.persistence.TrainingDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TrainingFragment extends Fragment {

    private ListView simpleList;
    private List<Training> trainigs;
    private int currentPosition = -1;

    public TrainingFragment() {
        super(R.layout.fragment_training);
    }

    @Override
    public void onStart() {

        super.onStart();

        trainigs = getTrainingsFromDB();
        List<String> trainingsTitle = new ArrayList<>();
        for (Training training : trainigs
        ) {
            trainingsTitle.add(training.title+":"+training.id_training);

        }
        simpleList = (ListView) getActivity().findViewById(R.id.training_list_view);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.training_list_item, R.id.training_item_text_view, trainingsTitle);
        simpleList.setAdapter(arrayAdapter);
        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Object listItem = simpleList.getItemAtPosition(position);
                currentPosition = position;
//          getActivity().getApplicationContext(),"Click ListItem Number"+listItem.getString(), Toast.LENGTH_LONG;
            }
        });


    }

    private List<Training> getTrainingsFromDB() {
        try {
            TrainingDB db = Room.databaseBuilder(getActivity().getApplicationContext(),
                    TrainingDB.class, "training-database").allowMainThreadQueries().build();
            TrainingDAO trainingDAO = db.trainingDAO();

            return trainingDAO.getAll();
        } catch (Exception ex) {
            Log.e("DBError", ex.toString());
        }
        return new ArrayList<>();
    }

    public Training getSelectedTraining() {

        if (currentPosition > 0) {
            return trainigs.get(currentPosition);
        }
        return null;
    }
}
