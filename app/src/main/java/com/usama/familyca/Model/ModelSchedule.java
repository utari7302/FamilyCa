package com.usama.familyca.Model;

public class ModelSchedule {
    private String to_be_done, description, time, cnic,date,timeStamp;

    public ModelSchedule() {
    }

    public ModelSchedule(String to_be_done, String description, String time, String cnic, String date, String timeStamp) {
        this.to_be_done = to_be_done;
        this.description = description;
        this.time = time;
        this.cnic = cnic;
        this.date = date;
        this.timeStamp = timeStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTo_be_done() {
        return to_be_done;
    }

    public void setTo_be_done(String to_be_done) {
        this.to_be_done = to_be_done;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }
}
