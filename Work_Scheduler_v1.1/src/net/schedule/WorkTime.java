package net.schedule;

import java.io.Serializable;

public class WorkTime implements Serializable {
    private Integer startHours;
    private Integer endHours;
    private Integer startMinutes;
    private Integer endMinutes;

    public WorkTime(Integer startHours, Integer startMinutes, Integer endHours, Integer endMinutes) {
        this.startHours = startHours;
        this.endHours = endHours;
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
    }

    public String toString(){
        return startHours + ":" + startMinutes + "-" + endHours +":"+endMinutes;
    }

    public Integer getStartHours() {
        return startHours;
    }

    public void setStartHours(Integer startHours) {
        this.startHours = startHours;
    }

    public Integer getEndHours() {
        return endHours;
    }

    public void setEndHours(Integer endHours) {
        this.endHours = endHours;
    }

    public Integer getStartMinutes() {
        return startMinutes;
    }

    public void setStartMinutes(Integer startMinutes) {
        this.startMinutes = startMinutes;
    }

    public Integer getEndMinutes() {
        return endMinutes;
    }

    public void setEndMinutes(Integer endMinutes) {
        this.endMinutes = endMinutes;
    }
}
