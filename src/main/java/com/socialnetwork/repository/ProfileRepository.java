package com.socialnetwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialnetwork.model.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
  Profile findOneByUserId(String userId);
}
