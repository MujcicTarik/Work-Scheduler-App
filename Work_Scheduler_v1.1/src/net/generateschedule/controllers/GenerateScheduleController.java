package net.generateschedule.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Labeled;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import net.schedule.Schedule;
import net.schedule.Shift;
import net.worker.DaysInWeek;
import net.worker.Worker;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;


import java.awt.Desktop;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;


public class GenerateScheduleController implements Initializable {

    @FXML
    private AnchorPane configureGenerationPane;

    @FXML
    private TextField numOfWorkersSaturday, numOfWorkersSunday, numOfWorkersNightShift;

    @FXML
    private JFXListView schedulesList;

    @FXML
    private JFXButton selectButton;

    @FXML
    private ComboBox selectedNightShiftComboBox;

    @FXML
    private JFXListView sheetsList;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private JFXButton generateButton;

    @FXML
    private JFXListView selectNumOfWorkersJFXListView;

    @FXML
    private TextField totalNumOfWorkersTextField;

    private ArrayList<String> fileNames = new ArrayList<String>();

    static String selectedSchedule;
    static Schedule temp;
    static int shiftFairPicker = 0;

    static ArrayList<Worker> sundayNightShiftWorkers = new ArrayList<Worker>();

    static HashMap<Shift, Integer> helpStaticNumOfWorkersPerShift = new HashMap<Shift, Integer>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectButton.setDisable(true);
        generateButton.setDisable(true);
        configureGenerationPane.setVisible(false);
        startDatePicker.setValue(null);
        //startDatePicker.setChronology();
        startDatePicker.setOnShowing(e -> Locale.setDefault(Locale.Category.FORMAT, Locale.UK));

        File dir = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String string = file.getName();
                fileNames.add(string);
            }
        }
        schedulesList.setItems(FXCollections.observableList(fileNames));
        schedulesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectButton.setDisable(false);
                //to handle a double click
                if (event.getClickCount() == 2) {
                    handleSelect();

                }
            }
        });


        //Date picker format change
        startDatePicker.setConverter(new StringConverter<LocalDate>() {
            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE);

            @Override
            public String toString(LocalDate localDate) {
                if (localDate == null)
                    return "";
                return dateTimeFormatter.format(localDate);
            }

            @Override
            public LocalDate fromString(String dateString) {
                if (dateString == null || dateString.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(dateString, dateTimeFormatter);
            }
        });

        sheetsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    File file = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules"
                            + File.separator + temp.getScheduleName() + File.separator + sheetsList.getSelectionModel().getSelectedItem());
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        });


    }

    @FXML
    void handleSelect() {
        configureGenerationPane.setVisible(true);
        startDatePicker.setValue(null);
        numOfWorkersNightShift.setText(null);
        numOfWorkersSaturday.setText(null);
        numOfWorkersSunday.setText(null);
        sheetsList.getItems().clear();
        selectNumOfWorkersJFXListView.getItems().clear();

        generateButton.setDisable(false);


        if (!(schedulesList.getSelectionModel().getSelectedItem() == null)) {
            selectedSchedule = schedulesList.getSelectionModel().getSelectedItem().toString();
            try {
                temp = Schedule.readSchedule(selectedSchedule);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            selectedNightShiftComboBox.setItems(FXCollections.observableList(temp.getShifts())); //to list shifts in the selectedNightShiftComboBox

            //List excel files in the sheetsList JFXListView
            File file = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + temp.getScheduleName() + File.separator);
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".xlsx")) {
                    sheetsList.getItems().add(f.getName());
                }
            }

            //List all the shifts and TextBoxes in the selectNumOfWorkersJFXListView
            //TODO SHOW THIS TO MIKE
            temp.getShifts().forEach(shift -> {
                TextField textField = new TextField();
                selectNumOfWorkersJFXListView.getItems().add(new Label(shift.getName(), textField));
                textField.setPrefWidth(50);
            });
            //to show the total number of workers, also disable this so it can't be edited
            totalNumOfWorkersTextField.setText(String.valueOf(temp.getWorkers().size()));
            totalNumOfWorkersTextField.setDisable(true);
        }
    }

    @FXML
    void handleGenerate(ActionEvent event) throws IOException {

        Integer totalNumber = Integer.parseInt(numOfWorkersSaturday.getText()) + Integer.parseInt(numOfWorkersSunday.getText()) + Integer.parseInt(numOfWorkersNightShift.getText());
        Integer limitNumber;
        ArrayList<Worker> workersSatSunNight = new ArrayList<Worker>(getSpecificDayWorkers(DaysInWeek.SUNDAY));
        for (Worker w : getSpecificDayWorkers(DaysInWeek.SATURDAY)) {
            if (!workersSatSunNight.contains(w))
                workersSatSunNight.add(w);
        }
        for (Worker w : getNightShiftWorkers()) {
            if (!workersSatSunNight.contains(w))
                workersSatSunNight.add(w);
        }
        limitNumber = workersSatSunNight.size();

        //this will be the sum of numbers of Workers that user typed in the JFXListView
        int sumWorkersPerShift = selectNumOfWorkersJFXListView.getItems()
                .stream()
                .mapToInt(element -> {
                    return Integer.parseInt(((TextField) ((Label) element).getGraphic()).getText().trim());
                })
                .sum();


        //If ALL fields are selected
        if (selectedNightShiftComboBox.getValue() == null || numOfWorkersNightShift.getText() == null || numOfWorkersSaturday.getText() == null
                || numOfWorkersSunday.getText() == null || startDatePicker.getValue() == null || sumWorkersPerShift != temp.getWorkers().size()) {

            if (sumWorkersPerShift != temp.getWorkers().size()) {
                openErrorWindow("../../fxml/errorMoreWorkersThanPossible.fxml");
            } else {
                openErrorWindow("../../fxml/errorShiftsEmpty.fxml");
            }
        }
        //if NumberOfNightWorkers isn't bigger then the actual number of night workers
        else if (Integer.parseInt(numOfWorkersNightShift.getText()) > getNightShiftWorkers().size() ||
                Integer.parseInt(numOfWorkersSaturday.getText()) > getSpecificDayWorkers(DaysInWeek.SATURDAY).size() ||
                Integer.parseInt(numOfWorkersSunday.getText()) > getSpecificDayWorkers(DaysInWeek.SUNDAY).size() ||
                totalNumber > limitNumber) {
            openErrorWindow("../../fxml/errorMoreWorkersThanPossible.fxml");
        } else {
//TODO ALGORITHM THAT CREATES THE SCHEDULE ==================================================================================================================
            Workbook workbook = new XSSFWorkbook();

            //for bold font
            XSSFFont boldFont = (XSSFFont) workbook.createFont();
            boldFont.setBold(true);

            XSSFCellStyle styleBold = (XSSFCellStyle) workbook.createCellStyle(); //CellStyle attribute that makes the text bold
            styleBold.setBorderTop(BorderStyle.valueOf((short) 1)); // single line border
            styleBold.setBorderBottom(BorderStyle.valueOf((short) 1)); // single line border
            styleBold.setBorderLeft(BorderStyle.valueOf((short) 1));
            styleBold.setBorderRight(BorderStyle.valueOf((short) 1));
            styleBold.setFont(boldFont);



            Sheet sheet = workbook.createSheet("week-" + startDatePicker.getValue().toString());
            Row row0 = sheet.createRow(0);
            firstRowGenerator(row0, startDatePicker.getValue(), workbook);
            for (int j = 0; j < temp.getWorkers().size(); j++) {
                Row row = sheet.createRow(j + 1);
                Cell cell = row.createCell(0);
                cell.setCellValue(temp.getWorkers().get(j).getFirstname() + " " + temp.getWorkers().get(j).getLastname());
                cell.setCellStyle(styleBold);
            }


            //creation of the hashMap that has which Shift should each Worker do Next Week
            HashMap<Worker, Shift> shiftNextWeekForWorkerHM = new HashMap<Worker, Shift>();
            temp.getWorkers().forEach(worker -> {
                shiftNextWeekForWorkerHM.put(worker, shiftThatWorkerShouldWorkNextWeek(worker));
            });


            ArrayList<Worker> generatedWorkersNextWeekNightShift;
            ArrayList<Worker> generatedWorkersSunday;
            ArrayList<Worker> generatedWorkersSaturday;

            ArrayList<Shift> shiftsWithoutNightShift = new ArrayList<Shift>(temp.getShifts());
            shiftsWithoutNightShift.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem());
            //NIGHT SHIFT
            generatedWorkersNextWeekNightShift = generateNightShift(sheet);

            HashMap<Worker, Shift> sundayHM = generateSunday(generatedWorkersNextWeekNightShift);
            HashMap<Worker, Shift> saturdayHM = generateSaturday(sundayHM.keySet().stream().collect(ArrayList::new, ArrayList::add, ArrayList::addAll), generatedWorkersNextWeekNightShift);
            HashMap<Worker, Shift> workDaysHM = new HashMap<Worker, Shift>();
            ArrayList<Worker> helpAllWorkers = new ArrayList<Worker>(temp.getWorkers());

            helpAllWorkers.removeAll(generatedWorkersNextWeekNightShift);//remove thisWeekNightShift and nextWeekNightShift
            helpAllWorkers.addAll(sundayNightShiftWorkers);//add NextWeekNightShift

            ArrayList<Worker> helpNightShiftWorkers = new ArrayList<Worker>(generatedWorkersNextWeekNightShift);
            ArrayList<Worker> helpSundayNSWorkers = new ArrayList<Worker>(sundayNightShiftWorkers);


            sundayNightShiftWorkers.clear(); //remove the elements from the static ArrayList so in the next schedule this list is empty as it needs to be
            helpAllWorkers.removeAll(sundayHM.keySet());//remove Sunday
            helpAllWorkers.removeAll(saturdayHM.keySet());//remove Saturday

            //make hash map for workdays that we will use in calculateNextShiftForWorkers
            helpAllWorkers.forEach(worker -> {
                workDaysHM.put(worker, shiftThatWorkerShouldWorkNextWeek(worker));
            });

            workDaysHM.forEach((w, s) -> {
                //System.out.println(w);
                //System.out.println(s);
            });


            HashMap<Worker, Shift> calculatedShiftsForWorkers = calculateNextShiftForWorkers(saturdayHM, sundayHM, workDaysHM);
            HashMap<Worker, Shift> helpCalculatedShiftsForWorkers = new HashMap<Worker, Shift>(calculatedShiftsForWorkers);

            //Write workers to schedule

            int offDayPicker = 0;

            //2. Write people that should be Working sunday (start from monday and add days)
            for (Worker w : sundayHM.keySet()) {
                for (Row row : sheet) {
                    if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                        for (int k = 1; k < 6; k++) { //4 times only because he works Sunday
                            if(k != ((offDayPicker % 5) + 1)) { //to skip the off day
                                Cell cell = row.createCell(k);
                                styleCell(sheet.getWorkbook(), cell, calculatedShiftsForWorkers.get(w).toString());
                                cell.setCellValue(calculatedShiftsForWorkers.get(w).toString());
                            }
                        }
                        offDayPicker++; //we increment the offDayPicker after we've added the worker to the excel schedule
                        //Write the shift in the Sunday cell
                        Cell cell = row.createCell(7);
                        styleCell(sheet.getWorkbook(), cell, calculatedShiftsForWorkers.get(w).toString());
                        cell.setCellValue(calculatedShiftsForWorkers.get(w).toString());
                    }

                }
            }


            //3. Write people that should be Working saturday (start from monday and add days)
            for (Worker w : saturdayHM.keySet()) {
                for (Row row : sheet) {
                    if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                        for (int k = 1; k < 6; k++) { //4 times only because he works Saturday
                            if(k != ((offDayPicker % 5) + 1)) {
                                Cell cell = row.createCell(k);
                                styleCell(sheet.getWorkbook(), cell, calculatedShiftsForWorkers.get(w).toString());
                                cell.setCellValue(calculatedShiftsForWorkers.get(w).toString());
                            }
                        }
                        offDayPicker++; //we increment the offDayPicker after we've added the worker to the excel schedule
                        //Write the shift in the Saturday cell
                        Cell cell = row.createCell(6);
                        styleCell(sheet.getWorkbook(), cell, calculatedShiftsForWorkers.get(w).toString());
                        cell.setCellValue(calculatedShiftsForWorkers.get(w).toString());
                    }
                }
            }



            //4. Write the workers that are left (off days Sat and Sun)
            for (Worker w : workDaysHM.keySet()) {
                for (Row row : sheet) {
                    if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                        Cell cellNight = row.getCell(7);
                        //if the worker is working night shift on Sunday we add him only 4 work days
                        if(cellNight != null && cellNight.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                            for (int k = 1; k < 5; k++) {
                                Cell cell = row.createCell(k);
                                styleCell(sheet.getWorkbook(), cell, calculatedShiftsForWorkers.get(w).toString());
                                cell.setCellValue(calculatedShiftsForWorkers.get(w).toString());
                            }
                        }
                        else {
                            for (int k = 1; k < 6; k++) {
                                Cell cell = row.createCell(k);
                                styleCell(sheet.getWorkbook(), cell, calculatedShiftsForWorkers.get(w).toString());
                                cell.setCellValue(calculatedShiftsForWorkers.get(w).toString());
                            }
                        }
                    }
                }
            }


            //OTHER
            //HashMap<Worker, Shift> resultHM = calculateNextShiftForWorkers();
            //------------SUNDAY------------- SUNDAY  -------------SUNDAY------------ SUNDAY  -------------SUNDAY------------ SUNDAY ------------------------- SUNDAY
            //generatedWorkersSunday = generateSunday(sheet, generatedWorkersNextWeekNightShift);
            //sub
            //generatedWorkersSaturday = generateSaturday(sheet, generatedWorkersSunday, generatedWorkersNextWeekNightShift);
            //radni dani
            //generateWorkDays(sheet);

            //--------------STYLE-------------------STYLE---------------------------------STYLE_-------------------

            //TODO TEST
            LinkedHashMap<Worker, Integer> hm = new LinkedHashMap<Worker, Integer>();
            for (Worker w : getSpecificDayWorkers(DaysInWeek.TUESDAY))
                hm.put(w, 0);


            /*for (int i = 1; i < 30; i++) {*/
            String s = "src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                    + File.separator + selectedSchedule + startDatePicker.getValue().minusDays(1 * 7) + ".xlsx";
            int firstShift = 0;
            int secondShift = 0;
            File file1 = new File(s);
            if (file1.exists()) {
                FileInputStream inputFile = new FileInputStream(file1);
                Workbook workbook1 = WorkbookFactory.create(inputFile);
                Sheet sheet1 = workbook1.getSheetAt(0);
                for (Worker w : temp.getWorkers()) {
                    Integer numberOfDaysWorked = hm.get(w);
                    for (Row row : sheet1) {
                        Cell cell0 = row.getCell(0);
                        if (cell0.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                            Cell cellDay = row.getCell(getIntegerFromDay(DaysInWeek.TUESDAY));
                            //Cell cellDay2 = row.getCell(getIntegerFromDay(DaysInWeek.SATURDAY));
                            if (cellDay != null && cellDay.getStringCellValue().equals(temp.getShifts().get(0).toString()) /*&& !cellDay.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())*/) {
                                firstShift++;
                            } else if (cellDay != null && cellDay.getStringCellValue().equals(temp.getShifts().get(1).toString()))
                                secondShift++;
                                /*if(cellDay2 != null) {
                                    numberOfDaysWorked++;
                                }*/
                                /*} else if (cellDay != null && !cellDay.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                                    numberOfDaysWorked++;
                                }*/
                            hm.replace(w, numberOfDaysWorked);
                        }
                    }
                }
                inputFile.close();
                workbook1.close();
                System.out.println("Prva smjena   " + firstShift);
                System.out.println("Druga smjena  " + secondShift);
                System.out.println("-----------------------------");
            }

            /*}*/

            for (Worker w : hm.keySet()) {
                // System.out.println(w + hm.get(w).toString());
            }


            //TODO TEST
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
            formatExcelDoc(workbook, helpSundayNSWorkers, helpCalculatedShiftsForWorkers, helpNightShiftWorkers);
            FileOutputStream file = new FileOutputStream("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                    + File.separator + selectedSchedule + startDatePicker.getValue() + ".xlsx");
            workbook.write(file);
            workbook.close();
            file.close();




        }
        //refreshing SheetList
        refreshSheetList();
    }

    private ArrayList<Worker> generateNightShift(Sheet sheet) throws IOException {
        ArrayList<Worker> generatedWorkers = new ArrayList<Worker>();
        ArrayList<Worker> workers = getNightShiftWorkers();
        LinkedHashMap<Worker, Integer> workerNightShiftHashMap = makeHashMapForWorkDays(getNightShiftWorkers(), DaysInWeek.MONDAY);
        LinkedHashMap<Worker, Integer> workerSundayDayShiftHashMap = makeHashMapForWorkDays(getNightShiftWorkers(), DaysInWeek.SUNDAY);
        LinkedHashMap<Worker, Integer> workerSaturdayDayShiftHashMap = makeHashMapForWorkDays(getNightShiftWorkers(), DaysInWeek.SATURDAY);

        //merging three Hash Maps
        //LinkedHashMap<Worker, Integer> mergedHashMap = mergeHashMaps(workerNightShiftHashMap, mergeHashMaps(workerSaturdayDayShiftHashMap, workerSundayDayShiftHashMap));
        LinkedHashMap<Worker, Integer> mergedHashMap = workerNightShiftHashMap;


        //goes through the Hash map and if there are new workers sets their shiftsNum to the smallest value in map
        valuesRefreshForHashMap(mergedHashMap);


        //cita ko je radio iz prosle sedmice nedjelju i njega stavlja da radi u sedmici za koju generisemo
        File f = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                + File.separator + selectedSchedule + startDatePicker.getValue().minusDays(7) + ".xlsx");
        if (f.exists()) {
            for (Worker w : getLastWeekNightWorkers(f)) {
                for (Row row : sheet) {
                    if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                        mergedHashMap.replace(w, mergedHashMap.get(w) + 1); //OVO BOGA MI MORA BITI OVDJE MATERE MI
                        for (int j = 1; j < 6; j++) {
                            Cell cell = row.createCell(j);
                            styleCell(sheet.getWorkbook(), cell, selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                            cell.setCellValue(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                        }
                        generatedWorkers.add(w);
                    }
                }
            }
        } else {
            //kreiramo prvi week schedule - nije bilo ni jednog rasporeda prije generisanja

            for (int j = 0; j < Integer.parseInt(numOfWorkersNightShift.getText()); j++) {
                boolean exit = false;
                Integer minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue();
                for (Worker w : mergedHashMap.keySet()) {
                    if (mergedHashMap.get(w) == minValue) {
                        mergedHashMap.replace(w, mergedHashMap.get(w) + 1);
                        for (Row row : sheet) {
                            if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                                for (int k = 1; k < 6; k++) {
                                    Cell cell = row.createCell(k);
                                    styleCell(sheet.getWorkbook(), cell, selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                                    cell.setCellValue(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                                    exit = true;
                                }
                                generatedWorkers.add(w);
                            }
                        }
                    }
                    if (exit == true)
                        break;
                }
            }
        }

        //save this weeks nightShift

        //For the next week who will work (who will work on Sunday this week)
        for (int j = 0; j < Integer.parseInt(numOfWorkersNightShift.getText()); j++) {
            boolean exit = false;
            Integer minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue();
            for (Worker w : mergedHashMap.keySet()) {
                if (mergedHashMap.get(w) == minValue) {
                    mergedHashMap.replace(w, mergedHashMap.get(w) + 1);
                    for (Row row : sheet) {
                        if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                            Cell cell = row.createCell(7);
                            styleCell(sheet.getWorkbook(), cell, selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                            cell.setCellValue(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                            generatedWorkers.add(w);
                            sundayNightShiftWorkers.add(w); //add it to the static ArrList that we use in our algorithm (we need to know who works Night on Sunday)
                            exit = true;
                        }
                    }
                }
                if (exit == true)
                    break;
            }
        }
        return generatedWorkers; //this returns the workers that work night shift this week and the workers that work on sunday night shift
    }

    private HashMap<Worker, Shift> generateSunday(ArrayList<Worker> generatedWorkersNextWeekNightShift) throws IOException {
        //ArrayList<Worker> generatedWorkers = new ArrayList<Worker>();
        ArrayList<Worker> workersSunday = getSpecificDayWorkers(DaysInWeek.SUNDAY); //we get all Workers that can work on Sunday
        workersSunday.removeAll(generatedWorkersNextWeekNightShift); //we remove the people that work this week night shift and the people that are in night shift on sunday

        LinkedHashMap<Worker, Integer> workersSundayHashMap = makeHashMapForWorkDays(workersSunday, DaysInWeek.SUNDAY);
        LinkedHashMap<Worker, Integer> workersSaturdayHashMap = makeHashMapForWorkDays(workersSunday, DaysInWeek.SATURDAY);
        LinkedHashMap<Worker, Integer> workersNightShiftHashMap = makeHashMapForWorkDays(workersSunday, DaysInWeek.MONDAY);

        //merge Sunday and Saturday and Night Shift Hash Maps
        LinkedHashMap<Worker, Integer> mergedHashMap = mergeHashMaps(workersSundayHashMap, mergeHashMaps(workersSaturdayHashMap, workersNightShiftHashMap));

        valuesRefreshForHashMap(mergedHashMap);

        Integer minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue(); //min value in hash map (Workers with min value need to work this sunday)
        //This is the list of workers that will work this sunday

        /*ArrayList<Worker> workersForThisSundayNew = new ArrayList<Worker>();
        for (int i = 0; i < Integer.parseInt(numOfWorkersSunday.getText()); i++) {
            Worker tmpWorker;
            workersForThisSundayNew.add( tmpWorker =
                    mergedHashMap.keySet()
                            .stream()
                            .filter(w -> minValue == mergedHashMap.get(w))
                            .findFirst().get());
            mergedHashMap.replace(tmpWorker, mergedHashMap.get(tmpWorker) + 1);  //update hashmap
            minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue();

        }*/

        /*ArrayList<Worker> workersForThisSunday = mergedHashMap.keySet()
                .stream()
                .filter(w -> minValue == mergedHashMap.get(w))
                //update minValue and update mergedHashMap

                .limit(Integer.parseInt(numOfWorkersSunday.getText()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        workersForThisSunday.forEach(w -> mergedHashMap.replace(w, mergedHashMap.get(w) + 1)); //update the hash map

        //

        workersForThisSunday.forEach(worker -> {
            workerShiftSundayHM.put(worker, shiftThatWorkerShouldWorkNextWeek(worker));
        });*/

        HashMap<Worker, Shift> workerShiftSundayHM = new HashMap<Worker, Shift>();
        //THIS IS TO WRITE THE FILES FROM THE OLD ALG\\

       /* for(int j = 0; j < Integer.parseInt(numOfWorkersSunday.getText()); j++) {

        }*/

        for (int j = 0; j < Integer.parseInt(numOfWorkersSunday.getText()); j++) {
            boolean exit = false;
            valuesRefreshForHashMap(mergedHashMap);
            minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue();

            for (Worker w : mergedHashMap.keySet()) {
                if (minValue == mergedHashMap.get(w)) {
                    mergedHashMap.replace(w, mergedHashMap.get(w) + 1);
                    //add worker to result HM
                    workerShiftSundayHM.put(w, shiftThatWorkerShouldWorkNextWeek(w));
                    exit = true;
                }
                if (exit == true)
                    break;
            }
        }
        return workerShiftSundayHM;
    }

    private HashMap<Worker, Shift> calculateNextShiftForWorkers(HashMap<Worker, Shift> saturdayHashMap,
                                                                HashMap<Worker, Shift> sundayHashMap, HashMap<Worker, Shift> workDayHashMap) {
        //OVDJE MOGU GENERISATI ISTOVREMENO SVE I SUN SAT I WORK DAYS
        //Potrebno je da posaljem 3 HashMape, jedna ce biti radnici Sunday, radnici Saturday, SviRadniciMinusNocniRadnici
        //Odredim smjene za RadniciSunday ovim dole alg
        //Odredim smjene za radniciSaturday istim algoritmom
        //Odredim smjene za radniciWorkDay algoritmom koji cu sutra smisliti
        /** najbolje mozda prolaziti kroz smjene jednu po jednu (sve smjene osim nocne)
         * dodavati radnike za tu smjenu (mislim da moze ovaj isti algoritam dole, ali treba provjeriti) i izbacivati ih iz hashMape
         * kad se napuni dovoljan broj radnika za tu smjenu, prelazimo na sljedecu smjenu SENIDE RESI :D */

        //treba skontati kako te sve podatke vratiti :OOOOOOOOOOOOOOOOO kurac ne treba, samo vratis SviRadniciMinusNocni hashMap i boli te k

        ArrayList<Shift> shiftsWithoutNightShift = new ArrayList<Shift>(temp.getShifts());
        shiftsWithoutNightShift.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem()); //deleting the night shift, so we have an arraylist of all other shifts

        HashMap<Worker, Shift> saturdayHelpHashMap = new HashMap<Worker, Shift>(saturdayHashMap);
        HashMap<Worker, Shift> sundayHelpHashMap = new HashMap<Worker, Shift>(sundayHashMap);
        HashMap<Worker, Shift> workDayHelpHashMap = new HashMap<Worker, Shift>(workDayHashMap);
        //merging 3 hash maps to the resultHashMap
        HashMap<Worker, Shift> resultHashMap = new HashMap<>(saturdayHashMap);
        resultHashMap.putAll(sundayHashMap);
        resultHashMap.putAll(workDayHelpHashMap);

        //TODO SUNDAY
        int numSunday = sundayHashMap.size();
        for (int shiftSequence = 0; numSunday != 0; numSunday--, shiftSequence++) {
            Shift currShift = shiftsWithoutNightShift.get(shiftSequence % shiftsWithoutNightShift.size());
            boolean isRemoved = false;
            for (Worker w : sundayHelpHashMap.keySet()) {
                if (currShift.getName().equals(sundayHelpHashMap.get(w).getName())) { //if you cand find Worker that should do currShift remove him from the helpHashMap
                    sundayHelpHashMap.remove(w);
                    isRemoved = true;
                    break; //exit out of the current loop because you've found the Worker that needs to work currShift
                }
            }
            if (isRemoved == false) { //if you didn't find anyone that needs to Work currShift -> add that shift to the first Worker
                List<Worker> keysAsList = new ArrayList<Worker>(sundayHelpHashMap.keySet());
                resultHashMap.replace(keysAsList.get(0), currShift); //replace the shift to the currShift
                sundayHelpHashMap.remove(keysAsList.get(0)); //remove the Worker since we've selected the shift for him
            }
        }


        //TODO SATURDAY
        int numSaturday = saturdayHashMap.size();
        for (int shiftSequence = 0; numSaturday != 0; numSaturday--, shiftSequence++) {
            Shift currShift = shiftsWithoutNightShift.get(shiftSequence % shiftsWithoutNightShift.size());
            boolean isRemoved = false;
            for (Worker w : saturdayHelpHashMap.keySet()) {
                if (currShift.getName().equals(saturdayHelpHashMap.get(w).getName())) { //if you cand find Worker that should do currShift remove him from the helpHashMap
                    saturdayHelpHashMap.remove(w);
                    isRemoved = true;
                    break; //exit out of the current loop because you've found the Worker that needs to work currShift
                }
            }
            if (isRemoved == false) { //if you didn't find anyone that needs to Work currShift -> add that shift to the first Worker
                List<Worker> keysAsList = new ArrayList<Worker>(saturdayHelpHashMap.keySet());
                resultHashMap.replace(keysAsList.get(0), currShift); //replace the shift to the currShift
                saturdayHelpHashMap.remove(keysAsList.get(0)); //remove the Worker since we've selected the shift for him
            }
        }

        //TODO WORK DAYS

        //CALCULATE WORK DAYS -> We must follow how many workers in which shift

        //CREATE the HashMap how much Workers every Shift needs to have
        //returns Shift from the label
        final Function<Object, Shift> getShiftFromLabel = x -> {
            return temp.getShifts().stream().filter(shift -> shift.getName().equals(((Label) x).getText())).findFirst().get();
        };
        //returns Integer from the TextField that is inside of a label
        final Function<Object, Integer> getIntFromLabelsTextField = element -> {
            return Integer.parseInt(((TextField) ((Label) element).getGraphic()).getText().trim());
        };
        //creation of the HashMap that reads the numbers that use typed in in the ListView and then makes a hash map key-Shift value-number
        HashMap<Shift, Integer> totalNumOfWorkersPerShiftHM =
                (HashMap<Shift, Integer>) selectNumOfWorkersJFXListView.getItems()
                        .stream()
                        .collect(Collectors.toMap(getShiftFromLabel, getIntFromLabelsTextField));
        totalNumOfWorkersPerShiftHM.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem());//We need to remove the nightShift from this HM

        helpStaticNumOfWorkersPerShift.putAll(totalNumOfWorkersPerShiftHM);

        //CREAT the HashMap that checks the current number of shifts that are added to a worker
        HashMap<Shift, Integer> currNumOfWorkersPerShiftHM = new HashMap<Shift, Integer>(totalNumOfWorkersPerShiftHM);
        for (Shift s : currNumOfWorkersPerShiftHM.keySet())
            currNumOfWorkersPerShiftHM.replace(s, 0); //we put all the values on 0
        //now we calculate how much workers there is already in each shift (from Sunday and Saturday) and we update the hashMap
        //sunday updateShiftCount
        numSunday = sundayHashMap.size();
        for (int shiftSequence = 0; numSunday != 0; numSunday--, shiftSequence++) {
            Shift currShift = shiftsWithoutNightShift.get(shiftSequence % shiftsWithoutNightShift.size()); //represents the shift that we currently updating in the HashMap
            currNumOfWorkersPerShiftHM.replace(currShift, currNumOfWorkersPerShiftHM.get(currShift) + 1);
        }
        //saturday updateShiftCount
        numSaturday = saturdayHashMap.size();
        for (int shiftSequence = 0; numSaturday != 0; numSaturday--, shiftSequence++) {
            Shift currShift = shiftsWithoutNightShift.get(shiftSequence % shiftsWithoutNightShift.size()); //represents the shift that we currently updating in the HashMap
            currNumOfWorkersPerShiftHM.replace(currShift, currNumOfWorkersPerShiftHM.get(currShift) + 1);
        }


        //NOW THE ALGORITHM STARTS FOR WORK DAYS
        for (Shift shift : currNumOfWorkersPerShiftHM.keySet()) { //selecting Workers for each shift
            int numOfWorkersToAdd = totalNumOfWorkersPerShiftHM.get(shift) - currNumOfWorkersPerShiftHM.get(shift);
            for (int i = 0; i < numOfWorkersToAdd; i++) {
                boolean isRemoved = false;
                for (Worker w : workDayHelpHashMap.keySet()) {
                    if (shift.getName().equals(workDayHelpHashMap.get(w).getName())) { //if you can find a Worker in HM that already should do that shift remove him from the HM
                        workDayHelpHashMap.remove(w);
                        isRemoved = true;
                        break; //exit out of the current loop because you've found the Worker that needs to work currShift
                    }
                }
                if (isRemoved == false) {
                    List<Worker> keysAsList = new ArrayList<Worker>(workDayHelpHashMap.keySet());
                    if(!keysAsList.isEmpty()) {
                        resultHashMap.replace(keysAsList.get(0), shift); //replace the shift to the currShift
                        workDayHelpHashMap.remove(keysAsList.get(0)); //remove the Worker since we've selected the shift for him
                    }
                    else {
                        System.out.println("PRAZNO KeysAsList 731");
                    }

                }
            }
        }

        return resultHashMap;
    }

    private HashMap<Worker, Shift> generateSaturday(ArrayList<Worker> generatedWorkersSunday, ArrayList<Worker> generatedWorkersNextWeekNightShift) throws IOException {
        //ArrayList<Worker> generatedWorkers = new ArrayList<Worker>();
        ArrayList<Worker> workersSaturday = getSpecificDayWorkers(DaysInWeek.SATURDAY);
        workersSaturday.removeAll(generatedWorkersNextWeekNightShift);
        workersSaturday.removeAll(generatedWorkersSunday);

        LinkedHashMap<Worker, Integer> workersSaturdayHashMap = makeHashMapForWorkDays(workersSaturday, DaysInWeek.SATURDAY);
        LinkedHashMap<Worker, Integer> workersSundayHashMap = makeHashMapForWorkDays(workersSaturday, DaysInWeek.SUNDAY);
        LinkedHashMap<Worker, Integer> workersNightShiftHashMap = makeHashMapForWorkDays(workersSaturday, DaysInWeek.MONDAY);


        //merge Sunday and Saturday and Night Shift Hash Maps
        LinkedHashMap<Worker, Integer> mergedHashMap = mergeHashMaps(workersSaturdayHashMap, mergeHashMaps(workersSundayHashMap, workersNightShiftHashMap));

        valuesRefreshForHashMap(mergedHashMap);

        /*//This is the list of workers that will work this Saturday
        ArrayList<Worker> workersForThisSaturday = mergedHashMap.keySet()
                .stream()
                .filter(w -> minValue == mergedHashMap.get(w))
                .limit(Integer.parseInt(numOfWorkersSaturday.getText()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        workersForThisSaturday.forEach(w -> mergedHashMap.replace(w, mergedHashMap.get(w) + 1)); //update the hash map
        System.out.println("MILENKO: " + workersForThisSaturday);

        //
        HashMap<Worker, Shift> workerShiftSaturdayHM = new HashMap<Worker, Shift>();
        workersForThisSaturday.forEach(worker -> {
            workerShiftSaturdayHM.put(worker, shiftThatWorkerShouldWorkNextWeek(worker));
        });*/

        Integer minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue(); //min value in hash map (Workers with min value need to work this sunday)

        //SAME SHIT AS IN SUNDAY
        HashMap<Worker, Shift> workerShiftSaturdayHM = new HashMap<Worker, Shift>();
        //THIS IS TO WRITE THE FILES FROM THE OLD ALG
        for (int j = 0; j < Integer.parseInt(numOfWorkersSaturday.getText()); j++) {
            boolean exit = false;
            valuesRefreshForHashMap(mergedHashMap);
            minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue();

            for (Worker w : mergedHashMap.keySet()) {
                if (minValue == mergedHashMap.get(w)) {
                    mergedHashMap.replace(w, mergedHashMap.get(w) + 1);
                    //add worker to result HM
                    workerShiftSaturdayHM.put(w, shiftThatWorkerShouldWorkNextWeek(w));
                    exit = true;
                }
                if (exit == true)
                    break;
            }
        }

        /*for (int j = 0; j < Integer.parseInt(numOfWorkersSaturday.getText()); j++) {
            boolean exit = false;
            Integer minValue = Collections.min(mergedHashMap.entrySet(), Map.Entry.comparingByValue()).getValue();

            for (Worker w : mergedHashMap.keySet()) {
                if (minValue == mergedHashMap.get(w)) {
                    mergedHashMap.replace(w, mergedHashMap.get(w) + 1);
                    for (Row row : sheet) {
                        if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                            Cell c = row.getCell(getIntegerFromDay(DaysInWeek.SATURDAY));
                            if (c == null && numOfDaysWorkedInWeek(w, sheet) < 5) {
                                Cell cell = row.createCell(getIntegerFromDay(DaysInWeek.SATURDAY));
                                String s = selectShiftForWorker(w);
                                styleCell(sheet.getWorkbook(), cell, s);
                                cell.setCellValue(s);
                                generatedWorkers.add(w);
                                exit = true;
                            }
                        }
                    }
                }
                if (exit == true)
                    break;
            }
        }*/

        return workerShiftSaturdayHM;

    }

    private void generateWorkDays(Sheet sheet) throws IOException {
        for (Worker w : temp.getWorkers()) {
            boolean exit = false;
            for (Row row : sheet) {
                if (row.getCell(0).getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                    if (numOfDaysWorkedInWeek(w, sheet) < 5) {
                        Cell cell7 = row.getCell(getIntegerFromDay(DaysInWeek.SUNDAY));
                        if (cell7 != null) {
                            if (cell7.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                                String s = selectShiftForWorker(w);
                                fillWorkDays(w, sheet, row, s);
                            } else {
                                fillWorkDays(w, sheet, row, cell7.getStringCellValue());
                            }
                            break;
                        }
                        Cell cell6 = row.getCell(getIntegerFromDay(DaysInWeek.SATURDAY));
                        if (cell6 != null) {
                            fillWorkDaysSaturday(w, sheet, row, cell6.getStringCellValue());
                            break;
                        }
                        if (cell6 == null && cell7 == null) {
                            String s = selectShiftForWorker(w);
                            fillWorkDays(w, sheet, row, s);
                        }
                    }
                }
            }
        }
    }

    private void fillWorkDays(Worker w, Sheet sheet, Row row, String string) throws IOException {
        for (int i = 1; numOfDaysWorkedInWeek(w, sheet) < 5 && i < 8; i++) {
            if (w.getWorkDays().contains(getDayFromInteger(i))) {
                Cell cell = row.createCell(i);
                styleCell(sheet.getWorkbook(), cell, string);
                cell.setCellValue(string);
            }
        }
    }

    private void fillWorkDaysSaturday(Worker w, Sheet sheet, Row row, String string) throws IOException {
        for (int i = 5; numOfDaysWorkedInWeek(w, sheet) < 5 && i > 0; i--) {
            if (w.getWorkDays().contains(getDayFromInteger(i))) {
                Cell cell = row.createCell(i);
                styleCell(sheet.getWorkbook(), cell, string);
                cell.setCellValue(string);
            }
        }
        if (numOfDaysWorkedInWeek(w, sheet) < 5) {
            Cell cell = row.createCell(6);
            styleCell(sheet.getWorkbook(), cell, string);
            cell.setCellValue(string);
        }
    }

    private ArrayList<Worker> getNightShiftWorkers() {
        ArrayList<Worker> workers = new ArrayList<Worker>();
        for (Worker w : temp.getWorkers()) {
            for (String s : w.getShifts()) {
                if (s.equals(((Shift) (selectedNightShiftComboBox.getSelectionModel().getSelectedItem())).getName())) {
                    workers.add(w);
                }
            }
        }
        workers.sort((e, v) -> e.getFirstname().compareTo(v.getFirstname()));
        return workers;
    }

    private void firstRowGenerator(Row row, LocalDate localDate, Workbook workbook) {
        //for bold font
        XSSFFont boldFont = (XSSFFont) workbook.createFont();
        boldFont.setBold(true);

        XSSFCellStyle styleBoldGreenBack = (XSSFCellStyle) workbook.createCellStyle(); //CellStyle attribute that makes the text bold
        styleBoldGreenBack.setBorderTop(BorderStyle.valueOf((short) 1)); // single line border
        styleBoldGreenBack.setBorderBottom(BorderStyle.valueOf((short) 1)); // single line border
        styleBoldGreenBack.setBorderLeft(BorderStyle.valueOf((short) 1));
        styleBoldGreenBack.setBorderRight(BorderStyle.valueOf((short) 1));

        Cell cellWorker = row.createCell(0);
        styleBoldGreenBack.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        styleBoldGreenBack.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellWorker.setCellValue("Worker");
        cellWorker.setCellStyle(styleBoldGreenBack);



        for (int i = 0; i < 7; i++) {
            Cell cell = row.createCell(i + 1);
            cell.setCellStyle(styleBoldGreenBack);
            cell.setCellValue(localDate.plusDays(i).toString());
        }
    }

    private ArrayList<Worker> getLastWeekNightWorkers(File file) throws IOException {
        ArrayList<Worker> workers = new ArrayList<Worker>();
        FileInputStream inputFile = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputFile);
        Sheet sheet = workbook.getSheetAt(0);
        for (Worker w : getNightShiftWorkers()) {
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                    Cell cell7 = row.getCell(7);
                    if (cell7 != null)
                        if (cell7.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                            workers.add(w);
                        }
                }
            }
        }
        workers.sort((e, v) -> e.getFirstname().compareTo(v.getFirstname()));
        return workers;
    }

    //function that gives back the ArrayList of workers that can work on specific day
    private ArrayList<Worker> getSpecificDayWorkers(DaysInWeek dayInWeek) {
        ArrayList<Worker> workers = new ArrayList<Worker>();
        for (Worker w : temp.getWorkers()) {
            if (w.getWorkDays().contains(dayInWeek))
                workers.add(w);
        }
        workers.sort((e, v) -> e.getFirstname().compareTo(v.getFirstname()));
        return workers;
    }

    //function that gives back the ArrayList of Workers that worked on specific day in a specific week/file
    private ArrayList<Worker> getSpecificDayWorkersFromWeek(File file, DaysInWeek dayInWeek) throws IOException {
        ArrayList<Worker> workers = new ArrayList<Worker>();
        FileInputStream inputFile = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputFile);
        Sheet sheet = workbook.getSheetAt(0);

        //Marking days from Enum with numbers for cells
        int cellNumber = getIntegerFromDay(dayInWeek);
        for (Worker w : getSpecificDayWorkers(dayInWeek)) {
            for (Row row : sheet) {
                Cell cell0 = row.getCell(0);
                if (cell0.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                    Cell dayCell = row.getCell(cellNumber);
                    if (dayCell != null)
                        workers.add(w);
                }
            }
        }
        workers.sort((e, v) -> e.getFirstname().compareTo(v.getFirstname()));
        return workers;
    }

    //help function to check if the user worked night shift in current WeekFile
    private int numOfDaysWorkedInWeek(Worker w, Sheet sheet) throws IOException {
        int numOfDaysWorked = 0;

        for (Row row : sheet) {
            Cell cell0 = row.getCell(0);
            if (cell0.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                for (int i = 1; i < 8; i++) {
                    Cell c = row.getCell(i);
                    if (c != null)
                        numOfDaysWorked++;
                }
            }
        }
        return numOfDaysWorked;
    }

    //help function to get the cellNumber from DaysInWeek Enum
    private int getIntegerFromDay(DaysInWeek dayInWeek) {
        if (dayInWeek == DaysInWeek.MONDAY)
            return 1;
        else if (dayInWeek == DaysInWeek.TUESDAY)
            return 2;
        else if (dayInWeek == DaysInWeek.WEDNESDAY)
            return 3;
        else if (dayInWeek == DaysInWeek.THURSDAY)
            return 4;
        else if (dayInWeek == DaysInWeek.FRIDAY)
            return 5;
        else if (dayInWeek == DaysInWeek.SATURDAY)
            return 6;
        else if (dayInWeek == DaysInWeek.SUNDAY)
            return 7;
        return 0;
    }

    private DaysInWeek getDayFromInteger(Integer integer) {
        if (integer == 1)
            return DaysInWeek.MONDAY;
        else if (integer == 2)
            return DaysInWeek.TUESDAY;
        else if (integer == 3)
            return DaysInWeek.WEDNESDAY;
        else if (integer == 4)
            return DaysInWeek.THURSDAY;
        else if (integer == 5)
            return DaysInWeek.FRIDAY;
        else if (integer == 6)
            return DaysInWeek.SATURDAY;
        else if (integer == 7)
            return DaysInWeek.SUNDAY;
        return null;
    }

    final Function<Object, Shift> getShiftFromLabel = x -> {
        return temp.getShifts().stream().filter(shift -> shift.getName().equals(((Label) x).getText())).findFirst().get();
    };

    //this function returns ArrayList of shifts after it converts them from ArrayList<String> that we get from Worker::getShifts
    private ArrayList<Shift> getWorkersShifts(Worker w) {
        //TODO SHOW TO MIKE RETARD
        return w.getShifts()
                .stream()
                .map(str -> temp.getShifts().stream().filter(shift -> str.equals(shift.getName())).findFirst().get())
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private Shift shiftThatWorkerShouldWorkNextWeek(Worker w) { //this function looks through the previous weeks and returns the shift which worker should do (deciding which one by following the seq x->y->z->x (should do y))
        //this way we can get
        ArrayList<Shift> workersShiftsWithoutNightShift = new ArrayList<Shift>(getWorkersShifts(w));
        if(workersShiftsWithoutNightShift.contains(selectedNightShiftComboBox.getSelectionModel().getSelectedItem())) {
            workersShiftsWithoutNightShift.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem()); //remove the night shift
        }
        ArrayList<Shift> helpArrayListShift = new ArrayList<Shift>(workersShiftsWithoutNightShift);

        if (workersShiftsWithoutNightShift.size() == 1) { //if worker can do only 1 shift after filtering the shifts in the above loop (line 673), return that shift
            return workersShiftsWithoutNightShift.get(0);
        } else {
            File folder = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                    + File.separator);
            for (int i = 1; i < folder.listFiles().length + 1; i++) { //going through previous weekSchedules, and removing shift that we found in the WeekSchedule from the shift array
                //this ensures that the Worker w will be working different shift each week
                String s = "src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                        + File.separator + selectedSchedule + startDatePicker.getValue().minusDays(i * 7) + ".xlsx";
                File file = new File(s);
                if (file.exists()) {
                    try {
                        FileInputStream inputFile = new FileInputStream(file);
                        Workbook workbook = WorkbookFactory.create(inputFile);
                        Sheet sheet = workbook.getSheetAt(0);
                        for (Row row : sheet) {
                            Cell cell0 = row.getCell(0);
                            if (cell0.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                                String shiftForRemoval = null;
                                for (int j = 1; j < 8 && shiftForRemoval == null; j++) {  //loop for going through the days in the WeekSchedule
                                    Cell cell = row.getCell(j);
                                    if (cell != null && !cell.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                                        //if not null and if it's not nightShift
                                        shiftForRemoval = cell.getStringCellValue();
                                    }
                                    if (workersShiftsWithoutNightShift.size() == 1) {
                                        inputFile.close();
                                        workbook.close();
                                        return workersShiftsWithoutNightShift.get(0);
                                    } else if (shiftForRemoval != null) {
                                        for (Shift shift : helpArrayListShift) {
                                            if (shift.toString().equals(shiftForRemoval))
                                                workersShiftsWithoutNightShift.remove(shift);
                                            if (workersShiftsWithoutNightShift.size() == 1)
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                        inputFile.close();
                        workbook.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {  //if previous week doesn't exist, use the shiftFairPicker to pick a shift for the user
                    return workersShiftsWithoutNightShift.get(shiftFairPicker++ % workersShiftsWithoutNightShift.size());
                }
            }
        }
        return null;


    }


    private String selectShiftForWorker(Worker w) throws IOException {
        ArrayList<Shift> shiftsWithoutNightShift = new ArrayList<Shift>(temp.getShifts());
        shiftsWithoutNightShift.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem()); //deleting the night shift, so we have an arraylist of all other shifts
        ArrayList<Shift> helpArrayListShift = new ArrayList<Shift>(shiftsWithoutNightShift);

        for (Shift s : helpArrayListShift) { // filtering through shiftsWithoutNightShift and removing the shifts that Worker w can't do
            if (shiftsWithoutNightShift.size() == 1)
                break;
            if (!w.getShifts().contains(s.getName())) {
                shiftsWithoutNightShift.remove(s);
            }
        }

        if (shiftsWithoutNightShift.size() == 1) { //if worker can do only 1 shift after filtering the shifts in the above loop (line 673), return that shift
            return shiftsWithoutNightShift.get(0).toString();
        } else {
            File folder = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                    + File.separator);
            for (int i = 1; i < folder.listFiles().length + 1; i++) { //going through previous weekSchedules, and removing shift that we found in the WeekSchedule from the shift array
                //this ensures that the Worker w will be working different shift each week
                String s = "src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                        + File.separator + selectedSchedule + startDatePicker.getValue().minusDays(i * 7) + ".xlsx";
                File file = new File(s);
                if (file.exists()) {
                    FileInputStream inputFile = new FileInputStream(file);
                    Workbook workbook = WorkbookFactory.create(inputFile);
                    Sheet sheet = workbook.getSheetAt(0);
                    for (Row row : sheet) {
                        Cell cell0 = row.getCell(0);
                        if (cell0.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                            String shiftForRemoval = null;
                            for (int j = 1; j < 8 && shiftForRemoval == null; j++) {  //loop for going through the days in the WeekSchedule
                                Cell cell = row.getCell(j);
                                if (cell != null && !cell.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                                    shiftForRemoval = cell.getStringCellValue();
                                }
                                if (shiftsWithoutNightShift.size() == 1) {
                                    inputFile.close();
                                    workbook.close();
                                    return shiftsWithoutNightShift.get(0).toString();
                                } else if (shiftForRemoval != null) {
                                    ArrayList<Shift> helpArrayListShift1 = new ArrayList<Shift>(shiftsWithoutNightShift);
                                    for (Shift shift : helpArrayListShift1) {
                                        if (shift.toString().equals(shiftForRemoval))
                                            shiftsWithoutNightShift.remove(shift);
                                        if (shiftsWithoutNightShift.size() == 1)
                                            break;
                                    }
                                }
                            }
                        }
                    }
                    inputFile.close();
                    workbook.close();
                } else {  //if previous week doesn't exist, use the shiftFairPicker to pick a shift for the user
                    return shiftsWithoutNightShift.get(shiftFairPicker++ % shiftsWithoutNightShift.size()).toString();
                }
            }
        }
        return null;
    }


    //help function that makes sure that Hash Map doesn't have any values except max and max-1
    public void valuesRefreshForHashMap(LinkedHashMap<Worker, Integer> hm) {
        for (Map.Entry<Worker, Integer> entry : hm.entrySet()) {
            if (entry.getValue() < Collections.max(hm.values()) - 1)
                entry.setValue(Collections.max(hm.values()) - 1);
        }
    }


    private LinkedHashMap<Worker, Integer> makeHashMapForWorkDays(ArrayList<Worker> workers, DaysInWeek day) throws IOException {
        LinkedHashMap<Worker, Integer> workedDays = new LinkedHashMap<Worker, Integer>();

        for (Worker w : workers) {
            workedDays.put(w, 0);
        }

        File folder = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                + File.separator);

        for (int i = 1; i < folder.listFiles().length; i++) {
            String s = "src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                    + File.separator + selectedSchedule + startDatePicker.getValue().minusDays(i * 7) + ".xlsx";
            File file = new File(s);
            if (file.exists()) {
                FileInputStream inputFile = new FileInputStream(file);
                Workbook workbook = WorkbookFactory.create(inputFile);
                Sheet sheet = workbook.getSheetAt(0);
                for (Worker w : workers) {
                    Integer numberOfDaysWorked = workedDays.get(w);
                    for (Row row : sheet) {
                        Cell cell0 = row.getCell(0);
                        if (cell0.getStringCellValue().equals(w.getFirstname() + " " + w.getLastname())) {
                            Cell cellDay = row.getCell(getIntegerFromDay(day));
                            if (cellDay != null && day == DaysInWeek.MONDAY && cellDay.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                                numberOfDaysWorked++;
                            } else if (cellDay != null && day != DaysInWeek.MONDAY && !cellDay.getStringCellValue().equals(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString())) {
                                numberOfDaysWorked++;
                            }
                            workedDays.replace(w, numberOfDaysWorked);
                        }
                    }
                }
                inputFile.close();
                workbook.close();
            }
        }
        return workedDays;
    }

    private LinkedHashMap<Worker, Integer> mergeHashMaps(LinkedHashMap<Worker, Integer> hm1, LinkedHashMap<Worker, Integer> hm2) {
        LinkedHashMap<Worker, Integer> result = new LinkedHashMap<Worker, Integer>();
        for (Worker w : hm1.keySet()) {
            if (hm2.keySet().contains(w)) {
                result.put(w, hm1.get(w) + hm2.get(w));
            }
        }
        return result;
    }

    private void areAllNumOfWorkersSelected(JFXListView listView) {
        selectNumOfWorkersJFXListView.setOnMouseEntered(event -> {
            selectNumOfWorkersJFXListView.getItems().forEach(element -> {
                String accessibleText = ((Label) element).getGraphic().getAccessibleText();
                if (accessibleText != null)
                    System.out.println(accessibleText);
                else
                    System.out.println("NULICA");
            });
        });


    }

    private void writeDaysToExcel(Row row, XSSFCellStyle styleBold) {
        Cell cell1 = row.createCell(1);
        cell1.setCellValue("MONTAG");
        cell1.setCellStyle(styleBold);

        Cell cell2 = row.createCell(2);
        cell2.setCellValue("DIENSTAG");
        cell2.setCellStyle(styleBold);

        Cell cell3= row.createCell(3);
        cell3.setCellValue("MITTWOCH");
        cell3.setCellStyle(styleBold);

        Cell cell4 = row.createCell(4);
        cell4.setCellValue("DONNERSTAG");
        cell4.setCellStyle(styleBold);

        Cell cell5 = row.createCell(5);
        cell5.setCellValue("FREITAG");
        cell5.setCellStyle(styleBold);

        Cell cell6 = row.createCell(6);
        cell6.setCellValue("SAMSTAG");
        cell6.setCellStyle(styleBold);

        Cell cell7 = row.createCell(7);
        cell7.setCellValue("SONNTAG");
        cell7.setCellStyle(styleBold);

    }

    private void formatExcelDoc(Workbook workbook, ArrayList<Worker> sundayNightShiftWorkers, HashMap<Worker, Shift> calculatedShiftsForWorkers, ArrayList<Worker> helpNightShiftWorkers) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Sheet oldSheet = workbook.getSheetAt(0);
        Row row0 = sheet.createRow(0);
        row0.createCell(0);

        /*final Function<Object, Integer> getIntFromLabelsTextField = element -> {
            return Integer.parseInt(((TextField) ((Label) element).getGraphic()).getText().trim());
        };*/


        /*HashMap<Shift, Integer> totalNumOfWorkersPerShiftHM =
                (HashMap<Shift, Integer>) selectNumOfWorkersJFXListView.getItems()
                        .stream()
                        .collect(Collectors.toMap(getShiftFromLabel, getIntFromLabelsTextField));
        totalNumOfWorkersPerShiftHM.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem());//We need to remove the nightShift from this HM*/
        //For font
        XSSFFont boldFont = (XSSFFont) wb.createFont();
        boldFont.setBold(true);

        XSSFCellStyle styleBold = (XSSFCellStyle) wb.createCellStyle();

        styleBold.setBorderTop(BorderStyle.valueOf((short) 1)); // single line border
        styleBold.setBorderBottom(BorderStyle.valueOf((short) 1)); // single line border
        styleBold.setBorderLeft(BorderStyle.valueOf((short) 1));
        styleBold.setBorderRight(BorderStyle.valueOf((short) 1));
        styleBold.setFont(boldFont);


        Cell firstCell = row0.createCell(0);
        styleBold.setFont(boldFont); //to set the style
        firstCell.setCellStyle(styleBold); //to style the first cell


        writeDaysToExcel(row0, styleBold);


        Row row1 = sheet.createRow(1);
        firstRowGenerator(row1, startDatePicker.getValue(), wb);
        row1.getCell(0).setCellValue("");
        int r = 2;
        for (int j = 0; j < Integer.parseInt(numOfWorkersNightShift.getText()); j++) { //write shift hours for Night Shift
            Row row = sheet.createRow(r++);
            for(int d = 0; d < 8; d++) {
                Cell cell = row.createCell(d);
                if(d == 0) { //if first cell make it bold and write the name of the shift
                    cell.setCellValue(selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString());
                    cell.setCellStyle(styleBold);
                }


            }
        }
        Row emptyRow = sheet.createRow(r); //empty line between the shifts
        for(int i = 0; i < 8; i++) {//to color the empty row in grey
            Cell cell = emptyRow.createCell(i);
            CellStyle cellStyle = wb.createCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(cellStyle);
        }
        r++;

        ArrayList<Shift> shiftsWithoutNightShift = new ArrayList<Shift>(temp.getShifts());
        shiftsWithoutNightShift.remove(selectedNightShiftComboBox.getSelectionModel().getSelectedItem());
        for(Shift s : shiftsWithoutNightShift) {
            int size = helpStaticNumOfWorkersPerShift.get(s);
            for(int i = 0; i < size; i++) {
                Row row = sheet.createRow(r++);
                for(int j = 0; j < 8; j++) {
                    Cell cell = row.createCell(j);
                    if(j == 0){
                        cell.setCellValue(s.toString()); //to write the name of the shift
                        cell.setCellStyle(styleBold); //to make the name of the shift bold
                    }




                }

            }
            Row emptyRow1 = sheet.createRow(r);
            for(int i = 0; i < 8; i++) {//to color the empty row in grey
                Cell cell = emptyRow1.createCell(i);
                CellStyle cellStyle = wb.createCellStyle();
                cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(cellStyle);
            }
            r++;
        }

        //Write SundayNightShiftWorkers in Sunday
        r = 2;
        for(Worker w : sundayNightShiftWorkers){
            sheet.getRow(r++).getCell(7).setCellValue(w.getFirstname() + " " + w.getLastname());
        }
        r++; //to skip the empty line

        //Write night shift workers (work days)


        for(int i = 1; i < temp.getWorkers().size() + 1; i++){ //We go through the workers in old format and find which shift are they working
            String workerName = workbook.getSheetAt(0).getRow(i).getCell(0).getStringCellValue();
            Worker temp = null;
            for(Worker w : calculatedShiftsForWorkers.keySet()){
                if((w.getFirstname() + " " + w.getLastname()).equals(workerName)) {
                    temp = w;
                }
            }
            Shift shift = calculatedShiftsForWorkers.get(temp);
            for(Row formatRow : sheet) {
                if(shift != null) {
                    Cell formatCell0 = formatRow.getCell(0);
                    Cell formatCell1 = formatRow.getCell(1);
                    if(!(formatCell0 == null || formatCell0.getCellType() == CellType.BLANK )
                            && formatCell0.getStringCellValue().equals(shift.toString())
                            && (formatCell1 == null || formatCell1.getCellType() == CellType.BLANK ) ) {
                        for(int j = 1; j < 8; j++) {
                            if(oldSheet.getRow(i).getCell(j) != null && oldSheet.getRow(i).getCell(j).getStringCellValue().equals(shift.toString())) {
                                formatRow.getCell(j).setCellValue(workerName);
                            }
                            else {
                                formatRow.getCell(j).setCellValue(" ");
                            }
                        }
                        break;
                    }
                }
            }




        }
        helpNightShiftWorkers.removeAll(sundayNightShiftWorkers);
            for(int i = 0; i < helpNightShiftWorkers.size(); i++) {
                Row row = sheet.getRow(i + 2);
                Worker w = helpNightShiftWorkers.get(i);
                for(int j = 1; j < 6; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(w.getFirstname() + " " + w.getLastname());
                }
            }




        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
        FileOutputStream file = new FileOutputStream("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + selectedSchedule
                + File.separator + "Formatted " + selectedSchedule + startDatePicker.getValue() + ".xlsx");
        wb.write(file);
        wb.close();
        file.close();
    }

    //TODO STYLING CELLS
    private void styleCell(Workbook workbook, Cell cell, String shift) {
        String nightShift = selectedNightShiftComboBox.getSelectionModel().getSelectedItem().toString();
        CellStyle style = workbook.createCellStyle();
        if (shift.equals(nightShift)) {
            style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (shift.equals(temp.getShifts().get(0).toString()) && !shift.equals(nightShift)) {
            style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (shift.equals(temp.getShifts().get(1).toString()) && !shift.equals(nightShift)) {
            style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (shift.equals(temp.getShifts().get(2).toString()) && !shift.equals(nightShift)) {
            style.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else if (shift.equals(temp.getShifts().get(3).toString()) && !shift.equals(nightShift)) {
            style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } else {
            style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        cell.setCellStyle(style);
    }

    @FXML
    void handleHoverButtonEnter() {
        double hover = 0.7;
        if (selectButton.isHover())
            selectButton.setOpacity(hover);
        if (generateButton.isHover())
            generateButton.setOpacity(hover);

    }

    @FXML
    void handleHoverButtonExit() {
        if (!selectButton.isHover())
            selectButton.setOpacity(1);
        if (!generateButton.isHover())
            generateButton.setOpacity(1);
    }

    private void refreshSheetList() {
        sheetsList.getItems().clear();
        if (temp != null) {
            File file = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + temp.getScheduleName() + File.separator);
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".xlsx")) {
                    sheetsList.getItems().add(f.getName());
                }
            }
        }
    }

    @FXML
    void refreshWindow() {


    }

    public void openErrorWindow(String s) throws IOException {
        try {
            Stage errorWindow = new Stage();
            errorWindow.setTitle("Error");
            errorWindow.initModality(Modality.APPLICATION_MODAL);
            errorWindow.initStyle(StageStyle.UNDECORATED);

            Parent loader = FXMLLoader.load(getClass().getResource(s));
            errorWindow.setScene(new Scene(loader));
            errorWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
