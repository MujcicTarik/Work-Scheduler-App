package net.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.schedule.Shift;
import net.worker.DaysInWeek;
import net.worker.Worker;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddNewWorkerController implements Initializable {

    @FXML
    private ImageView exitButton;
    @FXML
    private JFXListView shiftsListView;
    @FXML
    private JFXButton saveButton;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private CheckBox checkBoxMonday;

    @FXML
    private CheckBox checkBoxTuesday;

    @FXML
    private CheckBox checkBoxWednesday;

    @FXML
    private CheckBox checkBoxThursday;

    @FXML
    private CheckBox checkBoxFriday;

    @FXML
    private CheckBox checkBoxSaturday;

    @FXML
    private CheckBox checkBoxSunday;


    public void handleExitButton() throws IOException {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<CheckBox> items = new ArrayList<>();
        for(int i=0; i<NewScheduleController.getTemp().getShifts().size(); i++)
            items.add(new CheckBox(NewScheduleController.getTemp().getShifts().get(i).getName()));
        shiftsListView.getItems().addAll(items);
        checkBoxMonday.selectedProperty().set(true);
        checkBoxTuesday.selectedProperty().set(true);
        checkBoxWednesday.selectedProperty().set(true);
        checkBoxThursday.selectedProperty().set(true);
        checkBoxFriday.selectedProperty().set(true);
        checkBoxSaturday.selectedProperty().set(true);
        checkBoxSunday.selectedProperty().set(true);

    }

    public void handleSave() throws IOException {
        if(firstName.getText().equals("") || lastName.getText().equals("")){
            openErrorWindow();
        }
        else {
            if (checkSelectedShifts() && isAnyDaySelected()) { //add func check if any days selected
                ArrayList<Integer> ids = new ArrayList<Integer>();
                ArrayList<String> shifts = new ArrayList<String>();
                for (Object c : shiftsListView.getItems()) {
                    if (((CheckBox) c).isSelected()) {
                        for (Shift s : NewScheduleController.getTemp().getShifts()) {
                            if (s.getName().equals(((CheckBox) c).getText())) {
                                shifts.add(s.getName());
                                ids.add(s.getId());
                            }
                        }
                    }
                }

                ArrayList<DaysInWeek> days = new ArrayList<DaysInWeek>();
                NewScheduleController.getTemp().getWorkers().add(new Worker(firstName.getText(), lastName.getText(),
                        ids, returnSelectedDays(), shifts));
                ((Stage) exitButton.getScene().getWindow()).close();
            } else {
                openErrorWindow();
            }
        }
    }

    public boolean isAnyDaySelected(){
        if(checkBoxMonday.isSelected())
            return true;
        if(checkBoxTuesday.isSelected())
            return true;
        if(checkBoxWednesday.isSelected())
            return true;
        if(checkBoxThursday.isSelected())
            return true;
        if(checkBoxFriday.isSelected())
            return true;
        if(checkBoxSaturday.isSelected())
            return true;
        if(checkBoxSunday.isSelected())
            return true;
        else
            return false;
    }
    public ArrayList<DaysInWeek> returnSelectedDays(){
        ArrayList<DaysInWeek> days = new ArrayList<DaysInWeek>();
        if(checkBoxMonday.isSelected())
            days.add(DaysInWeek.MONDAY);
        if(checkBoxTuesday.isSelected())
            days.add(DaysInWeek.TUESDAY);
        if(checkBoxWednesday.isSelected())
            days.add(DaysInWeek.WEDNESDAY);
        if(checkBoxThursday.isSelected())
            days.add(DaysInWeek.THURSDAY);
        if(checkBoxFriday.isSelected())
            days.add(DaysInWeek.FRIDAY);
        if(checkBoxSaturday.isSelected())
            days.add(DaysInWeek.SATURDAY);
        if(checkBoxSunday.isSelected())
            days.add(DaysInWeek.SUNDAY);
        return days;
    }
    public boolean checkSelectedShifts(){
        for(Object c: shiftsListView.getItems()){
            if(((CheckBox) c).isSelected()){
                return true;
            }
        }
        return false;
    }

    public void openErrorWindow() throws IOException {
        try {
            System.out.println("TEST");
            Stage errorWindow = new Stage();
            errorWindow.setTitle("Error");
            errorWindow.initModality(Modality.APPLICATION_MODAL);
            errorWindow.initStyle(StageStyle.UNDECORATED);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/errorWorkerEmpty.fxml"));
            errorWindow.setScene(new Scene(loader.load()));
            errorWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
