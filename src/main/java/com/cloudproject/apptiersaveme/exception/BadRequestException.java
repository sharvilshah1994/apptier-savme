package com.cloudproject.apptiersaveme.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = -4451356147981308388L;

    private final String customUserMessage;

    private final Map<String, String> map;

    public BadRequestException(String s) {
        super(s);
        this.customUserMessage = "";
        this.map = new HashMap<>();
    }

    public BadRequestException(String s, String customUserMessage, Map<String, String> map) {
        super(s);
        this.customUserMessage = customUserMessage;
        this.map = map;
    }

    public BadRequestException(String s, String customUserMessage) {
        super(s);
        this.customUserMessage = customUserMessage;
        this.map = new HashMap<>();
    }

    public String getCustomUserMessage() {
        return customUserMessage;
    }

    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(map);
    }
}