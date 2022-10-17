package com.dev.realtimeappbackend.repository;

import com.dev.realtimeappbackend.domain.Notification;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface NotificationRepository extends ReactiveMongoRepository<Notification,String> {

}
