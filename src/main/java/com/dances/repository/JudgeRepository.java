package com.dances.repository;

import com.dances.entity.Judge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JudgeRepository extends JpaRepository<Judge, Long> {

    List<Judge> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);


}
