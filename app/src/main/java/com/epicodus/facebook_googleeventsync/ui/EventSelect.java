package com.epicodus.facebook_googleeventsync.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.epicodus.facebook_googleeventsync.R;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.EventListAdapter;
import butterknife.Bind;
import butterknife.ButterKnife;
import models.FacebookEvent;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class EventSelect extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
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
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).color(Color.BLACK).build());
        mGoogleEvents = getIntent().getStringArrayExtra("google events");
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        getEvents("placeholder");
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
                                mRecyclerView.setAdapter(new EventListAdapter(mEvents, new EventListAdapter.OnItemClickListener() {
                                    @Override public  void onItemClick(FacebookEvent event) {
                                        if (event.getSyncStatus() != "call made") {
                                            if (checkGoogleStatus()) {
                                                new MakeRequestTask(mCredential, event).execute(event);
                                                event.setSyncStatus("call made");
                                            }
                                        } else {
                                            System.out.println("Already syhcing");
                                        }
                                    }
                                }));
                                RecyclerView.LayoutManager layoutManager =
                                        new LinearLayoutManager(EventSelect.this);
                                mRecyclerView.setLayoutManager(layoutManager);
                                mRecyclerView.setHasFixedSize(true);
                            }

                        });
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "events.limit(100)");
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

                String startTime = eventJSON.getString("start_time");
                Date date;

                String pattern = "yyyy-MM-dd'T'HH:mm:ss";
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                try {
                    date = format.parse(startTime);
                    if (date.after(new Date())) {
                        String name = eventJSON.getString("name");
                        String rsvp = eventJSON.getString("rsvp_status");
                        String endTime = eventJSON.optString("end_time", "No end time provided");
                        String description = eventJSON.getString("description");
                        String place;
                        if (eventJSON.optJSONObject("place") != null) {
                            place = eventJSON.optJSONObject("place").optString("name", "No location provided");
                        } else {
                            place = "No location provided";
                        }
                        FacebookEvent event = new FacebookEvent(description, endTime, name, startTime, rsvp, place);
                        event.setSyncStatus(eventSyncStatus(event)); //The event is checking its own properties against the google list and determining what its status should be.
                        events.add(event);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Collections.reverse(events);
        return events;
    }

    private String eventSyncStatus(FacebookEvent fbEvent) {
        String facebookComparisonString = fbEvent.getName() + " ("+ fbEvent.getStartTime() + ")";
        if (!isUnique(facebookComparisonString, mGoogleEvents)) {
            return "duplicate";
        } else if (!isInTimeframe(fbEvent.getStartTime(), mGoogleEvents)) {
            return "danger zone";
        }
        else {
            return "sync me";
        }
    }

    private boolean isUnique(String fbEventString, String[] googleEventsStrings) {
        for (String event : googleEventsStrings) {
            if (event.equals(fbEventString)) {
                return false;
            }
        } return true;
    }

    private boolean isInTimeframe(String fbEventTime, String[] googleEvents) {
        boolean inTime = true;
        if (googleEvents.length != 101) {
            return inTime;
        } else {
                String event = googleEvents[100];
//                String eventTime = event.substring(event.length()-30, event.length()-1);
                Date googleDate = new Date();
                Date facebookDate = new Date();
                String pattern = "yyyy-MM-dd'T'HH:mm";
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                try {
                    googleDate = format.parse(event);
                    facebookDate = format.parse(fbEventTime);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (googleDate.before(facebookDate)) {
                    inTime = false;
                }
        } return inTime;
    }

    private boolean checkGoogleStatus() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(this.getApplicationContext(), "No network connection available.", Toast.LENGTH_SHORT).show();
        } else {
            return true;
        }
        return false;
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                checkGoogleStatus();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                } else {
                    checkGoogleStatus();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        checkGoogleStatus();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    checkGoogleStatus();
                }
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                EventSelect.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    private class MakeRequestTask extends AsyncTask<FacebookEvent, Void, Void> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential, FacebookEvent event) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Facebook event sync")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(FacebookEvent... params) {
            try {
                FacebookEvent syncEvent = params[0];
                Log.d ("Synching event: ", syncEvent.getName());

                Log.d("Start time", syncEvent.getStartTime());

                Event uploadEvent = new Event()
                        .setSummary(syncEvent.getName())
                        .setLocation(syncEvent.getPlace())
                        .setDescription(syncEvent.getDescription());

                DateTime startDateTime = new DateTime(syncEvent.getStartTime());
                EventDateTime startTime = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("America/Los_Angeles");
                uploadEvent.setStart(startTime);

                if (!syncEvent.getEndTime().equals("No end time provided")) {
                    DateTime endDateTime = new DateTime(syncEvent.getEndTime());
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone("America/Los_Angeles");
                    uploadEvent.setEnd(end);
                } else {
                    DateTime endDateTime = new DateTime(syncEvent.getStartTime());
                    DateTime endTimeOffset = new DateTime(endDateTime.getValue() + 3600 * 1000 * 3);

                    EventDateTime end = new EventDateTime()
                            .setDateTime(endTimeOffset)
                            .setTimeZone("America/Los_Angeles");
                    uploadEvent.setEnd(end);
                }

                    mService.events().insert("primary", uploadEvent).execute();

            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();

            Events events = mService.events().list("primary")
                    .setMaxResults(100)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }


        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleSyncActivity.REQUEST_AUTHORIZATION);
                } else {

                }
            } else {

            }
        }
    }
}