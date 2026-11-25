package com.dances.repository;

import com.dances.entity.ParticipantTournament;
import com.dances.model.SoloCoupleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantTournamentRepository extends JpaRepository<ParticipantTournament, Long> {

    List<ParticipantTournament> findByCategoryTournamentId(Long categoryId);

    List<ParticipantTournament> findByCategoryTournamentIdAndSoloCouple(
            Long categoryId, SoloCoupleEnum soloCouple);

    @Query("SELECT COALESCE(MAX(pt.startNumber), 0) FROM ParticipantTournament pt " +
            "WHERE pt.categoryTournament.id = :categoryId AND pt.soloCouple = :soloCouple")
    int findLastStartNumber(Long categoryId, SoloCoupleEnum soloCouple);

//    @Query("SELECT COALESCE(MAX(pt.startNumber), 0) FROM ParticipantTournament pt " +
//            "WHERE pt.categoryTournament.id = :categoryId AND pt.soloCouple = :soloCouple")
//    int findLastStartNumberDuringDay();

    boolean existsByCategoryTournamentIdAndParticipantId(Long categoryId, Long participantId);

    boolean existsByCategoryTournamentIdAndCoupleId(Long categoryId, Long coupleId);

    boolean existsByCategoryTournamentIdAndParticipantIdAndIdNot(Long categoryId, Long participantId, Long id);

    boolean existsByCategoryTournamentIdAndCoupleIdAndIdNot(Long categoryId, Long coupleId, Long id);

    long countByCategoryTournamentId(Long categoryTournamentId);
}
