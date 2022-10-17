package com.dev.realtimeappbackend.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;
    private String message;
    private String severity;

    public Notification(String message, String severity) {
        this.message = message;
        this.severity = severity;
    }
}
