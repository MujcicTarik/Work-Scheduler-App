package net.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;



public class ConfirmExitController {
    @FXML
    private ImageView exitButton;
    @FXML
    private JFXButton jfxExitButton;
    @FXML
    private JFXButton jfxCancelButton;

    public void handleExitButton() {
        ((Stage) exitButton.getScene().getWindow()).close();
    }

    public void handlejfxExitButton() {
        jfxExitButton.setOnMouseClicked(event -> System.exit(0));
    }

    public void handlejfxCancelButton() {
        ((Stage) jfxCancelButton.getScene().getWindow()).close();
    }

    public void handleHoverButtonEnter() {
        double hover = 0.7;
        if (jfxExitButton.isHover())
            jfxExitButton.setOpacity(hover);
        if (jfxCancelButton.isHover())
            jfxCancelButton.setOpacity(hover);
        if (exitButton.isHover())
            exitButton.setOpacity(hover);
    }
    public void handleHoverButtonExit() {
        if(!jfxExitButton.isHover())
            jfxExitButton.setOpacity(1);
        if(!jfxCancelButton.isHover())
            jfxCancelButton.setOpacity(1);
        if(!exitButton.isHover())
            exitButton.setOpacity(1);
    }


}
