package models;

import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        if (endTime.equals("No end time provided")) {
            return endTime;
        } else {
        String formattedEnd = endTime;
        formattedEnd = formattedEnd.substring(0, formattedEnd.length()-5) + ".000" + formattedEnd.substring(formattedEnd.length()-5, formattedEnd.length());
        formattedEnd = formattedEnd.substring(0, formattedEnd.length()-2) + ":" + formattedEnd.substring(formattedEnd.length()-2, formattedEnd.length());
        return formattedEnd;
        }
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        String formattedStart = startTime;
        formattedStart = formattedStart.substring(0, formattedStart.length()-5) + ".000" + formattedStart.substring(formattedStart.length()-5, formattedStart.length());
        formattedStart = formattedStart.substring(0, formattedStart.length()-2) + ":" + formattedStart.substring(formattedStart.length()-2, formattedStart.length());
        return formattedStart;
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

    public String getDisplayStart() {
        Date date = new Date();
        String pattern = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            date = format.parse(startTime);
            } catch (ParseException e) {
        e.printStackTrace();
    }
        String display = date.toString();
        display = display.substring(0, 16) + display.substring(23, display.length());
        return display;
    }

    public String getDisplayEnd() {
        if (this.getEndTime() == "No end time provided") {
            return "No end time provided";
        }
        Date date = new Date();
        String pattern = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            date = format.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String display = date.toString();
        display = display.substring(0, 16) + display.substring(23, display.length());
        return display;
    }
}

