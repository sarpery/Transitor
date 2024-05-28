package com.example.Frontend.PathStrategy;

import com.example.Controller.AlgoConnecter;
import javafx.scene.control.Label;

public class WalkingPath implements Path, AlgoConnecter {
    @Override
    public void calculateTime(String startText, String endText, Label timeLabel) throws Exception {
        calculateWalkTime(startText, endText, timeLabel);
    }

    private void calculateWalkTime(String startText, String endText, Label timeLabel) throws Exception{
        int time = (int) walkingTime(startText, endText);
        timeLabel.setText("Walking time: " + time + " min");
        System.out.println("Walking time: " + time);
    }
}

