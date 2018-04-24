package com.cloudproject.apptiersaveme.service;

import com.cloudproject.apptiersaveme.model.FirebaseResponse;
import com.google.gson.JsonArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

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

    public boolean sendNotification(String message, String[] registrationIds) throws JSONException, ExecutionException, InterruptedException {
        JSONObject body = new JSONObject();
        JsonArray jsonArray = new JsonArray();
        for (String id: registrationIds) {
            jsonArray.add(id);
        }
        body.put("dry_run", true);
        body.put("registration_ids", jsonArray);
        body.put("to", "AIzaSyACxDfGHTQ38nl7j-Q7K56gxo3ZmP9_ZKc");
        body.put("priority", "high");

        JSONObject notification = new JSONObject();
        notification.put("body", message);
        notification.put("title", "Please Help!");

        JSONObject data = new JSONObject();
        data.put("key1", "value1");
        data.put("key2", "value2");

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
}
