package com.dev.realtimeappbackend.controller;

import com.dev.realtimeappbackend.domain.Notification;
import com.dev.realtimeappbackend.event.Event;
import com.dev.realtimeappbackend.service.NotificationService;
import java.time.Duration;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notification")
@CrossOrigin("http://localhost:4200")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Flux<Notification> getAll() {
        return notificationService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Notification> create(@RequestBody Notification notification) {
        return notificationService.save(notification);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Notification> updateSeverity(@RequestBody Notification notification) {
        return notificationService.updateSeverity(notification, notification.getSeverity());
    }

    @DeleteMapping("/delete/{id}")
    public Mono<Void> delete(@PathVariable final String id) {
        return notificationService.deleteById(id);
    }

    @GetMapping("/events")
    public Flux<ServerSentEvent<Event>> getEventStatus() {
        return notificationService.listenEvents()
            .map(event -> ServerSentEvent.<Event>builder()
                .retry(Duration.ofSeconds(5))
                .event(event.getClass().getSimpleName()
                ).data(event).build());
    }
}
