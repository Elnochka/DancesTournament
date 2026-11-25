package com.dances.repository;

import com.dances.entity.CategoryParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryParticipantRepository extends JpaRepository<CategoryParticipant, Long> {
    boolean existsByNameCategory(String nameCategory);
}
