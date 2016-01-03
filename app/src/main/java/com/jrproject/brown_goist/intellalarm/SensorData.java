package com.jrproject.brown_goist.intellalarm;

public class SensorData implements Comparable<SensorData>{

    private int id;
    private int numEvents;
    private long timeStamp;
    private Status s;
    public enum Status {
        AWAKE,
        ASLEEP,
        RESTLESS
    }

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
        setStatusFlag();
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

    public void setStatusFlag() {
        if (numEvents >= 150) {
            s = Status.AWAKE;
        }
        if (numEvents >= 50 && numEvents < 150) {
            s = Status.RESTLESS;
        }
        if (numEvents >= 0 && numEvents < 50) {
            s = Status.ASLEEP;
        }
    }

    public Status getStatus() {
        return s;
    }

    @Override
    public int compareTo(SensorData another) {
        return Long.compare(timeStamp, another.getTimeStamp());
    }
}
