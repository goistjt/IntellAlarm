package com.jrproject.brown_goist.intellalarm;

public class SensorData implements Comparable<SensorData>{

    private int id;
    private int numEvents;
    private long timeStamp;

    public SensorData() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumEvents() {
        return numEvents;
    }

    public void setNumEvents(int numEvents) {
        this.numEvents = numEvents;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int compareTo(SensorData another) {
        return Long.compare(timeStamp, another.getTimeStamp());
    }
}
