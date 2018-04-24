package com.cloudproject.apptiersaveme.service;

import com.cloudproject.apptiersaveme.model.FirebaseResponse;
import com.cloudproject.apptiersaveme.util.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

@Component
public class NotificationHelperService {

    private final AndroidPushNotificationService androidPushNotificationService;

    private final Logger LOGGER = Logger.getLogger(NotificationHelperService.class.getName());

    @Autowired
    public NotificationHelperService(AndroidPushNotificationService androidPushNotificationService) {
        this.androidPushNotificationService = androidPushNotificationService;
    }

    public boolean sendNotification(String message, String[] registrationIds, String notificationKeyName)
            throws JSONException, ExecutionException, InterruptedException, IOException {
        String notificationKey = addNotificationKey(notificationKeyName, registrationIds);
        JSONObject body = new JSONObject();
        body.put("to", notificationKey);
        body.put("priority", "high");

        JSONObject notification = new JSONObject();
        notification.put("body", message);
        notification.put("title", "Please Help!");

        JSONObject data = new JSONObject();
        data.put("hello", "This is a Firebase Cloud Messaging Device Group Message!");

        body.put("notification", notification);
        body.put("data", data);

        HttpEntity<String> request = new HttpEntity<>(body.toString());
        LOGGER.info(body.toString());
        CompletableFuture<FirebaseResponse> pushNotification = androidPushNotificationService.send(request);
        CompletableFuture.allOf(pushNotification).join();

        FirebaseResponse firebaseResponse = pushNotification.get();
        if (firebaseResponse.getSuccess() == 1) {
            return true;
        }
        LOGGER.info("Error sending push notifications: " + firebaseResponse.toString());
        return false;
    }

    private String addNotificationKey(String notificationKeyName,
                                      String[] registrationIds)
            throws IOException, JSONException {
        URL url = new URL("https://fcm.googleapis.com/fcm/googlenotification");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);

        // HTTP request header
        con.setRequestProperty("project_id", Constants.PROJECT_ID);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "key="+Constants.FIREBASE_SERVER_KEY);
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");
        con.connect();

        // HTTP request
        JSONObject data = new JSONObject();
        data.put("operation", "create");
        data.put("notification_key_name", notificationKeyName);
        data.put("registration_ids", new JSONArray(Arrays.asList(registrationIds)));
        data.put("id_token", Constants.CLIENT_ID_TOKEN);

        LOGGER.info(data.toString());
        OutputStream os = con.getOutputStream();
        os.write(data.toString().getBytes("UTF-8"));
        os.close();

        // Read the response into a string
        InputStream is = con.getInputStream();
        String responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        is.close();

        // Parse the JSON string and return the notification key
        JSONObject response = new JSONObject(responseString);
        return response.getString("notification_key");
    }
}
