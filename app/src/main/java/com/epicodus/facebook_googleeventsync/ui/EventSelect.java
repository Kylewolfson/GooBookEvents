package com.epicodus.facebook_googleeventsync.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.epicodus.facebook_googleeventsync.R;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import models.Event;
import okhttp3.Response;

public class EventSelect extends AppCompatActivity {
    public static final String TAG = EventSelect.class.getSimpleName();

    @Bind(R.id.listView) ListView mListView;

    public ArrayList<Event> mEvents = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_select);
        ButterKnife.bind(this);
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

                        EventSelect.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                String[] eventNames = new String[mEvents.size()];
                                for (int i = 0; i < eventNames.length; i++) {
                                    eventNames[i] = mEvents.get(i).getName();
                                }

                                ArrayAdapter adapter = new ArrayAdapter(EventSelect.this, android.R.layout.simple_list_item_1, eventNames);
                                mListView.setAdapter(adapter);
                            }

                        });
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events");
        request.setParameters(parameters);
        request.executeAsync();
        };

    public ArrayList<Event> processResults(GraphResponse response) {
        ArrayList<Event> events = new ArrayList<>();

        try {
            JSONObject fbJSON  = response.getJSONObject();
            JSONObject drilldownJSON = fbJSON.getJSONObject("events");
            JSONArray eventsJSON = drilldownJSON.getJSONArray("data");
                for (int i = 0; i < eventsJSON.length(); i++) {
                    JSONObject eventJSON = eventsJSON.getJSONObject(i);

                    String name = eventJSON.getString("name");
                    String rsvp = eventJSON.getString("rsvp_status");
                    String endTime = eventJSON.optString("end_time", "No end time provided");
                    String startTime = eventJSON.getString("start_time");
                    String description = eventJSON.getString("description");
                    String place = eventJSON.getJSONObject("place").toString();
                    Event event = new Event(description, endTime, name, startTime, rsvp, place);
                    events.add(event);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }
}


