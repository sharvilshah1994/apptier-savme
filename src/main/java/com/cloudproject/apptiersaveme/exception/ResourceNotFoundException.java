package com.cloudproject.apptiersaveme.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -1238233032222415200L;

    private final String customUserMessage;

    private final Map<String, String> map;

    public ResourceNotFoundException(String s) {
        super(s);
        this.customUserMessage = "";
        this.map = new HashMap<>();
    }

    public ResourceNotFoundException(String s, String customUserMessage, Map<String, String> map) {
        super(s);
        this.customUserMessage = customUserMessage;
        this.map = map;
    }

    public String getCustomUserMessage() {
        return customUserMessage;
    }

    public Map<String, String> getMap() {
        return Collections.unmodifiableMap(map);
    }
}
