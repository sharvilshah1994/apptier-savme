package com.cloudproject.apptiersaveme.service;

import com.cloudproject.apptiersaveme.model.FirebaseResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class AndroidPushNotificationService {

    private static final String FIREBASE_SERVER_KEY = "AAAAqYNJJVM:APA91bGGTnjCzYOuMGz3hA6TdzhlF46_zteT1A3gb-3_dTtlX-fmaAujYYh0U7HShYsSw5EYYNfHvktPFWA_7iSBDcd4edakfMV44T5mtBoaSobnKp2B0CNgR0IGn94TIZmR6zajgr2m";

    @Async
    public CompletableFuture<FirebaseResponse> send(HttpEntity<String> entity) {

        RestTemplate restTemplate = new RestTemplate();

        ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + FIREBASE_SERVER_KEY));
        interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
        restTemplate.setInterceptors(interceptors);

        FirebaseResponse firebaseResponse = restTemplate.postForObject("https://fcm.googleapis.com/fcm/send", entity, FirebaseResponse.class);

        return CompletableFuture.completedFuture(firebaseResponse);
    }
}
