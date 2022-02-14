package net.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ErrorMoreWorkersThanPossibleController {

    @FXML
    private ImageView errorExitButton;

    @FXML
    void handleExitButton(MouseEvent event) {
        ((Stage)errorExitButton.getScene().getWindow()).close();
    }

    @FXML
    void handleHoverButtonEnter(MouseEvent event) {
        if(errorExitButton.isHover())
            errorExitButton.setOpacity(0.7);
    }

    @FXML
    void handleHoverButtonExit(MouseEvent event) {
        if(!errorExitButton.isHover())
            errorExitButton.setOpacity(1);
    }

}
