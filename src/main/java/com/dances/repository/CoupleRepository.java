package com.dances.repository;

import com.dances.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    boolean existsByNameCouple(String name);
}
