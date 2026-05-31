package com.socialnetwork.post.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private int id;

  @Column(name = "created_by_id", nullable = false)
  int createdByProfileId;

  String imageUrl;
  String caption;
  private Date createdAt;

  @OneToMany(mappedBy = "post")
  @JsonProperty("comments")
  private List<Comment> comments;

  @ElementCollection
  @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
  @Column(name = "profile_id")
  Set<Integer> userLikesProfileIds;
}
