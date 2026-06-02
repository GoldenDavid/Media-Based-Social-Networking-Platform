package com.socialnetwork.notification.model;

import java.util.Date;

import com.socialnetwork.common.event.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    // Decoupled from Profile Entity
    @Column(name = "from_profile_id", nullable = false)
    private int fromProfileId;

    @Column(name = "to_profile_id", nullable = false)
    private int toProfileId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @NotNull
    private Date createdAt;

    // Decoupled from Post Entity
    @Column(name = "post_id", nullable = false)
    private int postId;
}
