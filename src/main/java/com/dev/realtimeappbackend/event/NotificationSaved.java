package com.dev.realtimeappbackend.event;

import com.dev.realtimeappbackend.domain.Notification;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class NotificationSaved implements Event {

    private Notification notification;
}
