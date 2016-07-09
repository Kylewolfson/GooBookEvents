package com.epicodus.facebook_googleeventsync.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.epicodus.facebook_googleeventsync.R;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import models.Event;
import okhttp3.Response;

public class EventSelect extends AppCompatActivity {
    public static final String TAG = EventSelect.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_select);
        getEvents("placeholder");
    }

    private void getEvents(String params) {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        String jsonData = response.toString();
                        Log.v(TAG, jsonData);
                        mEvents = processResults(response);
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events");
        request.setParameters(parameters);
        request.executeAsync();
        };
    public ArrayList<Event> processResults(Response response) {
        ArrayList<Event> events = new ArrayList<>();

        try {
            String jsonData = response.body().string();
            if (response.isSuccessful()) {
                JSONObject fbJSON = new JSONObject(jsonData);
                JSONArray eventsJSON = fbJSON.getJSONArray("events");
                for (int i = 0; i < eventsJSON.length(); i++) {
                    JSONObject eventJSON = eventsJSON.getJSONObject(i);

                    String name = eventJSON.getString("name");
                    String rsvp = eventJSON.getString("rsvp_status");
                    String endTime = eventJSON.getString("end_time");
                    String startTime = eventJSON.getString("start_time");
                    String description = eventJSON.getString("description");
                    String place = eventJSON.getJSONArray("place").get(0).toString();
                    Event event = new Event(description, endTime, name, startTime, rsvp, place);
                    events.add(event);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }


}


