package models;

import org.parceler.Parcel;

/**
 * Created by Kyle on 7/8/2016.
 */

@Parcel
public class FacebookEvent {
     String description;
     String endTime;
     String name;
     String startTime;
     String rsvp;
     String place;
     String syncStatus;

    public FacebookEvent() {}


    public FacebookEvent(String description, String endTime, String name, String startTime, String rsvp, String place) {
        this.description = description;
        this.endTime = endTime;
        this.name = name;
        this.startTime = startTime;
        this.rsvp = rsvp;
        this.place = place;
        this.syncStatus = "danger zone";
    }

    public String getDescription() {
        return description;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getRsvp() {
        return rsvp;
    }

    public String getPlace() {
        return place;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String status) {
        this.syncStatus = status;
    }
}

