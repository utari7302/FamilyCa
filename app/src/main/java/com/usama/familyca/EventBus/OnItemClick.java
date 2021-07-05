package com.usama.familyca.EventBus;

public class OnItemClick {

    boolean isClick;
    String date;
    String time;

    public OnItemClick(boolean isClick, String date, String time) {
        this.isClick = isClick;
        this.date = date;
        this.time = time;
    }

    public boolean isClick() {
        return isClick;
    }

    public void setClick(boolean click) {
        isClick = click;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
