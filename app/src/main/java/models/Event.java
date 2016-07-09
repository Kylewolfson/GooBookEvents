package models;

/**
 * Created by Kyle on 7/8/2016.
 */
public class Event {
    private String mDescription;
    private String mEndTime;
    private String mName;
    private String mStartTime;
    private String mRsvp;
    private String mPlace;


    public Event(String description, String endTime, String name, String startTime, String rsvp, String place) {
        this.mDescription = description;
        this.mEndTime = endTime;
        this.mName = name;
        this.mStartTime = startTime;
        this.mRsvp = rsvp;
        this.mPlace = place;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public String getName() {
        return mName;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public String getRsvp() {
        return mRsvp;
    }

    public String getPlace() {
        return mPlace;
    }

}

