package net.schedule;

import java.io.Serializable;

public class Shift implements Serializable {
    private static int numberOfShifts;
    private int id;
    private String name;
    private WorkTime workTime;


    public Shift(String name, int startHours, int startMinutes, int endHours, int endMinutes) {
        this.id = numberOfShifts++;
        this.name = name;
        workTime = new WorkTime(startHours, startMinutes, endHours, endMinutes);
    }

    @Override
    public String toString(){
        return name + " " + workTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public WorkTime getWorkTime() {
        return workTime;
    }

    public void setWorkTime(WorkTime workTime) {
        this.workTime = workTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
