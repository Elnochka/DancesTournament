package com.dances.repository;

import com.dances.entity.Mark;
import com.dances.model.RoundEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarkRepository extends JpaRepository<Mark, Long> {

    List<Mark> findByCategoryTournamentIdAndRound(Long categoryId, RoundEnum round);

//    List<Mark> findByParticipantTournamentIdAndRound(Long participantId, RoundEnum round);

    List<Mark> findByCategoryTournamentId(Long categoryId);

    List<Mark> findByJudge_NameJudge(String nameJudge);

    List<Mark> findByCategoryTournamentIdAndRoundAndPassedTrue(
            Long categoryTournamentId,
            RoundEnum round
    );

//    List<Mark> findByCategoryTournamentIdAndRoundAndJudgeId(
//            Long categoryTournamentId,
//            RoundEnum round,
//            Long judgeId
//    );

    long countByCategoryTournamentIdAndRoundAndJudgeIdAndDanceIdAndPassedTrue(
            Long categoryTournamentId,
            RoundEnum round,
            Long judgeId,
            Long danceId

    );

    boolean existsByCategoryTournamentIdAndParticipantTournamentId(Long categoryId, Long participantId);

//    long countByCategoryTournamentIdAndRoundAndJudgeIdAndPassedTrue(
//            Long categoryTournamentId,
//            RoundEnum round,
//            Long judgeId
//
//    );

    @Modifying
    @Query("""
          update Mark m
          set m.locked = true
          where m.categoryTournament.id = :categoryId
            and m.round = :round
        """)
    void lockRound(Long categoryId, RoundEnum round);

    @Modifying
    @Query("""
          update Mark m
          set m.locked = true
          where m.categoryTournament.id = :categoryId
            and m.round = :round and m.dance.id = :danceId
        """)
    void lockDanceRound(Long categoryId, RoundEnum round, Long danceId);
}
