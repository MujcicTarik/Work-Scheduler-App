package net.schedule;

import net.worker.Worker;

import java.io.*;
import java.util.ArrayList;


public class Schedule implements Serializable{
    private String scheduleName;
    private  ArrayList<Worker> workers = new ArrayList<Worker>();
    private  ArrayList<Shift> shifts = new ArrayList<Shift>();


    public void loadShifts() throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("src" + File.separator +"net" + File.separator + "data" + File.separator + "shifts.txt"));
            String line;
            while((line = br.readLine()) != null){
                Shift shift = new Shift(line.split("#")[1],
                        Integer.parseInt(line.split("#")[2]), Integer.parseInt(line.split("#")[3]),
                        Integer.parseInt(line.split("#")[4]), Integer.parseInt(line.split("#")[5]));
                boolean hasShift = false;
                for(Shift s : shifts){
                    if(s.getId() == shift.getId())
                        hasShift=true;
                }
                if(!hasShift)
                shifts.add(shift);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
    public void loadWorkers() throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("src" + File.separator +"net" + File.separator + "data" + File.separator + "workers.txt"));
            String line;
            while((line = br.readLine()) != null){
                ArrayList<Integer> idShifts = new ArrayList<Integer>();
                for(int i = 4; i<line.split("#").length; i++){
                    idShifts.add(Integer.parseInt(line.split("#")[i]));
                }
                Worker worker = new Worker(line.split("#")[1],
                        line.split("#")[2], idShifts, new ArrayList<>(), new ArrayList<>());
                boolean hasWorker = false;
                for(Worker w : workers){
                    if(w.getId() == worker.getId())
                        hasWorker = true;
                }
                if(hasWorker == false)
                workers.add(worker);

            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    // call only if we are sure that file with this name doesn't already exist
    public void writeSchedule() throws IOException {
        this.setScheduleName(this.scheduleName.trim());
        String filePath = "src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + this.scheduleName + File.separator + this.scheduleName + ".txt";
        FileOutputStream fos = new FileOutputStream(filePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.close();
        fos.close();
    }
    public static Schedule readSchedule(String scheduleName) throws IOException, ClassNotFoundException {
        String filePath = "src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + scheduleName + File.separator + scheduleName + ".txt";
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Schedule schedule = (Schedule) ois.readObject();
        fis.close();
        ois.close();
        return schedule;
    }
    public  ArrayList<Worker> getWorkers() {
        return workers;
    }

    public  void setWorkers(ArrayList<Worker> workers) {
        this.workers = workers;
    }

    public  ArrayList<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(ArrayList<Shift> shifts) {
        this.shifts = shifts;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }
}
