package com.dev.realtimeappbackend.service;

import com.dev.realtimeappbackend.domain.Notification;
import com.dev.realtimeappbackend.event.Event;
import com.dev.realtimeappbackend.event.NotificationDeleted;
import com.dev.realtimeappbackend.event.NotificationSaved;
import com.dev.realtimeappbackend.repository.NotificationRepository;
import com.mongodb.client.model.changestream.OperationType;
import java.util.Objects;
import org.bson.BsonObjectId;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ReactiveMongoTemplate mongoTemplate;

    public NotificationService(NotificationRepository notificationRepository,
        ReactiveMongoTemplate mongoTemplate) {
        this.notificationRepository = notificationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Flux<Notification> getAll() {
        return notificationRepository.findAll();
    }

    public Mono<Notification> save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Mono<Notification> findById(String id) {
        return notificationRepository.findById(id);
    }

    public Mono<Notification> updateSeverity(Notification notification, String severity) {
        return findById(notification.getId()).flatMap(notification1 -> {
            notification1.setSeverity(severity);
            return notificationRepository.save(notification1);
        });
    }

    public Mono<Void> deleteById(String id) {
        return findById(id).flatMap(notificationRepository::delete);
    }

    public Notification toResource(Notification notification) {
        return notification;
    }

    public Flux<Event> listenEvents() {
        final ChangeStreamOptions changeStreamOptions = ChangeStreamOptions.builder()
            .returnFullDocumentOnUpdate()
            .filter(Aggregation.newAggregation(
                Aggregation.match(
                    Criteria.where("operationType")
                        .in(OperationType.INSERT.getValue(),
                            OperationType.REPLACE.getValue(),
                            OperationType.UPDATE.getValue(),
                            OperationType.DELETE.getValue())
                )
            ))
            .build();
        return mongoTemplate.changeStream("notifications", changeStreamOptions, Notification.class)
            .map(this::toEvent);
    }

    public Event toEvent(final ChangeStreamEvent<Notification> changeStreamEvent) {
        final Event event;
        switch (Objects.requireNonNull(changeStreamEvent.getOperationType())) {
            case DELETE://notification to document deleted
                event = new NotificationDeleted().setId(((BsonObjectId) changeStreamEvent.getRaw()
                    .getDocumentKey()
                    .get("_id")).getValue().toString());
                break;
            case INSERT:
            case UPDATE:
            case REPLACE:
                event = new NotificationSaved().setNotification(
                    toResource(changeStreamEvent.getBody()));
                break;
            default:
                throw new UnsupportedOperationException(
                    String.valueOf(changeStreamEvent.getOperationType()));

        }
        return event;
    }
}
