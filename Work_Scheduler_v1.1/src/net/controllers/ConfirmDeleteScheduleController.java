package net.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class ConfirmDeleteScheduleController  {

    @FXML
    private ImageView exitButton;
    @FXML
    private JFXButton jfxDeleteButton, jfxCancelButton;


    public void handleExitButton(){
        ((Stage) exitButton.getScene().getWindow()).close();
    }

    public void handlejfxDeleteButton(){
        EditScheduleController.isDeleteSelected = true;
        System.out.println("Delete is:" + EditScheduleController.isDeleteSelected);
        ((Stage) jfxDeleteButton.getScene().getWindow()).close();
    }

    public void handlejfxCancelButton(){
        ((Stage) jfxCancelButton.getScene().getWindow()).close();
    }

    public void handleHoverButtonEnter() {
        double hover = 0.7;
        if(jfxDeleteButton.isHover())
            jfxDeleteButton.setOpacity(hover);
        if(jfxCancelButton.isHover())
            jfxCancelButton.setOpacity(hover);
        if(exitButton.isHover())
            exitButton.setOpacity(hover);
    }
    public void handleHoverButtonExit() {
        if(!jfxDeleteButton.isHover())
            jfxDeleteButton.setOpacity(1);
        if(!jfxCancelButton.isHover())
            jfxCancelButton.setOpacity(1);
        if(!exitButton.isHover())
            exitButton.setOpacity(1);
    }

}
