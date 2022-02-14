package net.controllers;


import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.schedule.Shift;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddNewShiftEDITController implements Initializable {

    @FXML
    private ComboBox startTimeHComboBox, startTimeMComboBox;
    @FXML
    private ComboBox endTimeHComboBox, endTimeMComboBox;
    @FXML
    private ImageView exitButton;
    @FXML
    private JFXButton saveButton;
    @FXML
    private TextField shiftName;

    String hours[] = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11",
            "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    String minutes[] = {"00", "15", "30", "45"};

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        startTimeHComboBox.getItems().addAll(hours);
        startTimeMComboBox.getItems().addAll(minutes);
        endTimeHComboBox.getItems().addAll(hours);
        endTimeMComboBox.getItems().addAll(minutes);
    }

    public void handleSave() throws IOException {
        if (shiftName.getText().isEmpty() || startTimeHComboBox.getSelectionModel().isEmpty() || startTimeMComboBox.getSelectionModel().isEmpty()
                || endTimeHComboBox.getSelectionModel().isEmpty()   || endTimeMComboBox.getSelectionModel().isEmpty()) {  //to make sure no fields are empty
            Stage newWindow = new Stage();
            newWindow.setTitle("ERROR!");
            newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
            newWindow.initStyle(StageStyle.UNDECORATED);
            //Create view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/errorShiftsEmpty.fxml"));
            //Set view in window
            newWindow.setScene(new Scene(loader.load()));
            //Launch
            newWindow.show();
        }
        else { //creation and handling of the shift
            boolean exists = false;
            if(!(EditScheduleController.getSchedule().getShifts().isEmpty())) {
                for (Shift s : EditScheduleController.getSchedule().getShifts()) {
                    if (s.getName().equals(shiftName.getText())) {
                        exists = true;
                        //Error Window
                        Stage errorWindow = new Stage();
                        errorWindow.setTitle("Error");
                        errorWindow.initModality(Modality.APPLICATION_MODAL);
                        errorWindow.initStyle(StageStyle.UNDECORATED);

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/errorScheduleAlreadyExists.fxml"));
                        errorWindow.setScene(new Scene(loader.load()));
                        errorWindow.show();
                    }
                }
            }
            if(exists == false) {
                Shift shift = new Shift(shiftName.getText(),
                        Integer.parseInt(startTimeHComboBox.getSelectionModel().getSelectedItem().toString()),
                        Integer.parseInt(startTimeMComboBox.getSelectionModel().getSelectedItem().toString()),
                        Integer.parseInt(endTimeHComboBox.getSelectionModel().getSelectedItem().toString()),
                        Integer.parseInt(endTimeMComboBox.getSelectionModel().getSelectedItem().toString()));
                EditScheduleController.getSchedule().getShifts().add(shift);
                ((Stage) exitButton.getScene().getWindow()).close();
            }
            //Check if the name already exists (if there is - error)
            //else
            //Saving the shfit and closing the window

            //closing the window


        }
    }
    public void handleExitButton() throws IOException {
        Stage s = (Stage) exitButton.getScene().getWindow();
        s.close();
    }

    //Opacity hover change
    public void handleHoverButtonEnter() {
        if(exitButton.isHover())
            exitButton.setOpacity(0.7);
        if(saveButton.isHover())
            saveButton.setOpacity(0.7);
    }
    public void handleHoverButtonExit() {
        if(!exitButton.isHover())
            exitButton.setOpacity(1);
        if(!saveButton.isHover())
            saveButton.setOpacity(1);
    }
}
