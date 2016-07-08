package com.epicodus.facebook_googleeventsync.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.epicodus.facebook_googleeventsync.R;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.AccessToken;

import org.json.JSONObject;

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
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events");
        request.setParameters(parameters);
        request.executeAsync();
        };
    }


