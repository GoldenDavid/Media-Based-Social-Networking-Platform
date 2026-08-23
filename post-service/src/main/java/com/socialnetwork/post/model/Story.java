package com.socialnetwork.post.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "story")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Story {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "created_by_profile_id", nullable = false)
  private int createdByProfileId;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Column(name = "created_at", nullable = false)
  private Date createdAt;
}
