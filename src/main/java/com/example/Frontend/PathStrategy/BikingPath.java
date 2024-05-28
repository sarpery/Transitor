package com.example.Frontend.PathStrategy;

import com.example.Controller.AlgoConnecter;
import javafx.scene.control.Label;

public class BikingPath implements Path, AlgoConnecter {
    @Override
    public void calculateTime(String startText, String endText, Label timeLabel) throws Exception {
        calculateBikeTime(startText, endText, timeLabel);
    }

    private void calculateBikeTime(String startText, String endText, Label timeLabel) throws Exception{
        int time = (int) cyclingTime(startText, endText);
        timeLabel.setText("Biking time: " + time + " min");
        System.out.println("Biking time: " + time);
    }
}
