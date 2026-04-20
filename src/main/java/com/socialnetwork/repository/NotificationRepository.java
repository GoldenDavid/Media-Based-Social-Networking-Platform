package com.socialnetwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialnetwork.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
