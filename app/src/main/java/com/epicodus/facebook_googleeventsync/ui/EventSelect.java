package com.epicodus.facebook_googleeventsync.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.epicodus.facebook_googleeventsync.R;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import adapters.EventListAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import models.FacebookEvent;

public class EventSelect extends AppCompatActivity {

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.syncButton)
    Button mSyncButton;
    private EventListAdapter mAdapter;

    public ArrayList<FacebookEvent> mEvents = new ArrayList();
    private String[] mGoogleEvents;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EventSelect.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_select);
        ButterKnife.bind(this);
        mGoogleEvents = getIntent().getStringArrayExtra("google events");

        getEvents("placeholder");
        mSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventSelect.this, GoogleSyncActivity.class);
                intent.putExtra("events", Parcels.wrap(mEvents));
                startActivity(intent);
            }
        });
    }

    private void getEvents(String params) {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mEvents = processResults(response);


                        EventSelect.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mAdapter = new EventListAdapter(getApplicationContext(), mEvents);
                                mRecyclerView.setAdapter(mAdapter);
                                RecyclerView.LayoutManager layoutManager =
                                        new LinearLayoutManager(EventSelect.this);
                                mRecyclerView.setLayoutManager(layoutManager);
                                mRecyclerView.setHasFixedSize(true);
                            }

                        });
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events");
        request.setParameters(parameters);
        request.executeAsync();
    }

    ;

    public ArrayList<FacebookEvent> processResults(GraphResponse response) {
        ArrayList<FacebookEvent> events = new ArrayList<>();

        try {
            JSONObject fbJSON = response.getJSONObject();
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
                FacebookEvent event = new FacebookEvent(description, endTime, name, startTime, rsvp, place);
                event.setSyncStatus(eventSyncStatus(event)); //The event is checking its own properties against the google list and determining what its status should be.
                events.add(event);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return events;
    }

    private String eventSyncStatus(FacebookEvent fbEvent) {
        if (!isUnique(fbEvent.getName(), mGoogleEvents)) {
            return "duplicate";
        } else if (!isInTimeframe(fbEvent.getStartTime(), mGoogleEvents)) {
            return "danger zone";
        }
        else {
            return "sync me";
        }
    }

    private boolean isUnique(String fbEvent, String[] googleEvents) {
        boolean unique = true;

        for (String event : googleEvents) {
            if (event.equals(fbEvent)) {
                unique = false;
            }
        } return unique;
    }

    private boolean isInTimeframe(String fbEvent, String[] googleEvents) {
        boolean unique = true;

        for (String event : googleEvents) {
            if (event.equals(fbEvent)) {
                unique = false;
            }
        } return unique;
    }

}