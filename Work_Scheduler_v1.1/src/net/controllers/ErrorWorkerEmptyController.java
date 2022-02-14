package net.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ErrorWorkerEmptyController {

    @FXML
    private ImageView errorExitButton;


    public void handleExitButton() {
        ((Stage) errorExitButton.getScene().getWindow()).close();
    }

    //Opacity change on hover
    public void handleHoverButtonEnter() {
        if(errorExitButton.isHover())
            errorExitButton.setOpacity(0.7);
    }
    public void handleHoverButtonExit() {
        if(!errorExitButton.isHover())
            errorExitButton.setOpacity(1);
    }

}
