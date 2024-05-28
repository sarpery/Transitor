package com.example.Frontend.PathStrategy;

import com.example.Frontend.UIController;
import com.gluonhq.maps.MapView;
import javafx.scene.control.Label;

public class TransportPath implements Path {
    private UIController uiController;

    public TransportPath(UIController uiController) {
        this.uiController = uiController;
    }

    @Override
    public void calculateTime(String startText, String endText, Label timeLabel) throws Exception {
        uiController.pathDrawBus(startText, endText);
    }
}
