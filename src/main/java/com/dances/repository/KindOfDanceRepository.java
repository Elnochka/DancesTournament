package com.dances.repository;

import com.dances.entity.KindOfDance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KindOfDanceRepository extends JpaRepository<KindOfDance, Long> {

    boolean existsByNameKindOfDance(String name);
}
