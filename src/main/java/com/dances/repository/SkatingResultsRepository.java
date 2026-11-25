package com.dances.repository;

import com.dances.entity.SkatingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkatingResultsRepository extends JpaRepository<SkatingResult, Long> {

    List<SkatingResult> findByCategoryTournamentIdOrderByFinalPlace(Long categoryId);
    void deleteByCategoryTournamentId(Long categoryId);
    List<SkatingResult> findByCategoryTournamentId(Long categoryId);

}
