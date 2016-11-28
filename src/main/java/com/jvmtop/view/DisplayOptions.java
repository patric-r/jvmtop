package com.jvmtop.view;


public class DisplayOptions {
    private boolean isCSV = false;
    private boolean displayTS = false;
    private Integer width = 0;

    public DisplayOptions(boolean isCSV, boolean displayTS, Integer width) {
        this.isCSV = isCSV;
        this.displayTS = displayTS;
        this.width = width;
    }

    public boolean isCSV() {
        return isCSV;
    }

    public void setCSV(boolean CSV) {
        isCSV = CSV;
    }

    public boolean isDisplayTS() {
        return displayTS;
    }

    public void setDisplayTS(boolean displayTS) {
        this.displayTS = displayTS;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
