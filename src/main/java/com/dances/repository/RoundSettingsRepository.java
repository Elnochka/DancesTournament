package com.dances.repository;

import com.dances.entity.RoundSettings;
import com.dances.model.RoundEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundSettingsRepository extends JpaRepository<RoundSettings, Long> {

    Optional<RoundSettings> findByCategoryTournamentIdAndRound(
            Long categoryId,
            RoundEnum round
    );

    List<RoundSettings> findByCategoryTournamentId(Long categoryId);
}
