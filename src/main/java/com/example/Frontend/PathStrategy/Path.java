package com.example.Frontend.PathStrategy;

import javafx.scene.control.Label;

public interface Path {
    void calculateTime(String startText, String endText, Label timeLabel) throws Exception;
}

