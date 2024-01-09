package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class PacemakerFragment extends Fragment {
    public PacemakerFragment() {
        super(R.layout.fragment_pacemaker);
    }

    public String getDistance() {
        EditText distanceText = (EditText) getView().findViewById(R.id.set_distance);
        return distanceText.getText().toString();

    }

    public String getTime() {
        EditText timeText = (EditText) getView().findViewById(R.id.set_time);
        return timeText.getText().toString();
    }

}

