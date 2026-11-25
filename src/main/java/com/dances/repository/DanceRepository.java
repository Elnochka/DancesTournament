package com.dances.repository;

import com.dances.entity.Dance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanceRepository extends JpaRepository<Dance, Long> {

    List<Dance> findByKindOfDanceId(Long kindOfDanceId);

}
