package com.dances.repository;

import com.dances.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByNameClub(String name);
}
