package net.worker;

import net.schedule.Shift;

import java.io.Serializable;
import java.util.ArrayList;

public class Worker implements Serializable {
    private int id;
    private String firstname;
    private String lastname;
    private int numberOfWorkingSaturdays;
    private ArrayList<Integer> idOfShifts;
    private ArrayList<DaysInWeek> workDays;
    private ArrayList<String> shifts = new ArrayList<String>();

    public Worker(String firstname, String lastname, ArrayList<Integer> idOfShifts, ArrayList<DaysInWeek> workDays, ArrayList<String> shifts) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.idOfShifts = idOfShifts;
        this.workDays = workDays;
        this.shifts = shifts;
    }

    @Override
    public String toString(){
        String fullName = firstname + " " + lastname;
        return String.format("%s",fullName) + " " + shifts;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getNumberOfWorkingSaturdays() {
        return numberOfWorkingSaturdays;
    }

    public void setNumberOfWorkingSaturdays(int numberOfWorkingSaturdays) {
        this.numberOfWorkingSaturdays = numberOfWorkingSaturdays;
    }

    public ArrayList<Integer> getIdOfShifts() {
        return idOfShifts;
    }

    public void setIdOfShifts(ArrayList<Integer> idOfShifts) {
        this.idOfShifts = idOfShifts;
    }

    public ArrayList<DaysInWeek> getWorkDays() {
        return workDays;
    }

    public void setWorkDays(ArrayList<DaysInWeek> workDays) {
        this.workDays = workDays;
    }

    public ArrayList<String> getShifts() {
        return shifts;
    }

    public void setShifts(ArrayList<String> shifts) {
        this.shifts = shifts;
    }
}
