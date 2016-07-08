package services;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Kyle on 7/8/2016.
 */
public class FacebookEventService {

    public static void eventRetreival(String params, Callback callback) {

        OkHttpClient client = new OkHttpClient.Builder().build();

//        HttpUrl.Builder urlBuilder = HttpUrl.parse("graph.facebook.com/me/events").newBuilder();
        String url = "http://graph.facebook.com/me/events";

        Request request = new Request.Builder().url(url).build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

}
