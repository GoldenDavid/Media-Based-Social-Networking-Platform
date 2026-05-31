package com.socialnetwork.profile.repository;

import com.socialnetwork.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {

    Profile findOneByUserId(String userId);
}
