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

public class EditWorkerController implements Initializable {

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
        firstName.setText(EditScheduleController.getSelectedWorker().getFirstname());
        lastName.setText(EditScheduleController.getSelectedWorker().getLastname());
        for(DaysInWeek d : EditScheduleController.getSelectedWorker().getWorkDays()){
            if(d.equals(DaysInWeek.MONDAY))
                checkBoxMonday.selectedProperty().set(true);
            if(d.equals(DaysInWeek.TUESDAY))
                checkBoxTuesday.selectedProperty().set(true);
            if(d.equals(DaysInWeek.WEDNESDAY))
                checkBoxWednesday.selectedProperty().set(true);
            if(d.equals(DaysInWeek.THURSDAY))
                checkBoxThursday.selectedProperty().set(true);
            if(d.equals(DaysInWeek.FRIDAY))
                checkBoxFriday.selectedProperty().set(true);
            if(d.equals(DaysInWeek.SATURDAY))
                checkBoxSaturday.selectedProperty().set(true);
            if(d.equals(DaysInWeek.SUNDAY))
                checkBoxSunday.selectedProperty().set(true);
        }

        List<CheckBox> items = new ArrayList<>();
        for(int i=0; i<EditScheduleController.getSchedule().getShifts().size(); i++)
            items.add(new CheckBox(EditScheduleController.getSchedule().getShifts().get(i).getName()));
        shiftsListView.getItems().addAll(items);
    }

    public void handleSave() throws IOException {
        if(firstName.getText().equals("") || lastName.getText().equals("")){
            openErrorWindow();
        }
        else {
            if (checkSelectedShifts() && isAnyDaySelected()) {
                ArrayList<String> shifts = new ArrayList<String>();
                ArrayList<Integer> ids = new ArrayList<Integer>();
                for (Object c : shiftsListView.getItems()) {
                    if (((CheckBox) c).isSelected()) {
                        for (Shift s : EditScheduleController.getSchedule().getShifts()) {
                            if (s.getName().equals(((CheckBox) c).getText())) {
                                shifts.add(s.getName());
                                ids.add(s.getId());
                            }
                        }
                    }
                }
                EditScheduleController.getSelectedWorker().setFirstname(firstName.getText());
                EditScheduleController.getSelectedWorker().setLastname(lastName.getText());
                EditScheduleController.getSelectedWorker().setIdOfShifts(ids);
                EditScheduleController.getSelectedWorker().setWorkDays(returnSelectedDays());
                EditScheduleController.getSelectedWorker().setShifts(shifts);
                ((Stage) exitButton.getScene().getWindow()).close();
            } else {
                openErrorWindow();
            }
        }
    }

    public boolean isAnyDaySelected(){
        if(checkBoxMonday.isSelected())
            return true;
        else if(checkBoxTuesday.isSelected())
            return true;
        else if(checkBoxWednesday.isSelected())
            return true;
        else if(checkBoxThursday.isSelected())
            return true;
        else if(checkBoxFriday.isSelected())
            return true;
        else if(checkBoxSaturday.isSelected())
            return true;
        else if(checkBoxSunday.isSelected())
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
