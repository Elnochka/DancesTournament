package com.dances.repository;

import com.dances.entity.FinalMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinalMarkRepository extends JpaRepository<FinalMark, Long> {

    List<FinalMark> findByCategoryTournamentId(Long categoryId);

    List<FinalMark> findByJudge_NameJudge(String nameJudge);

    @Query("""
    select count(distinct m.participantTournament.id)
    from FinalMark m
    where m.categoryTournament.id = :categoryId
    """)
    long countDistinctParticipantsByCategory(Long categoryId);

//    long countByCategoryTournamentId(Long categoryId);

    boolean existsByCategoryTournamentIdAndDanceIdAndPlace(
            Long categoryId,
            Long danceId,
            int place
    );

    boolean existsByCategoryTournamentIdAndJudgeIdAndPlace(
            Long categoryId,
            Long judgeId,
            int place
    );

    boolean existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlace(
            Long categoryId,
            Long judgeId,
            Long danceId,
            int place
    );

    boolean existsByCategoryTournamentIdAndJudgeIdAndDanceIdAndPlaceAndIdNot(
            Long categoryId,
            Long judgeId,
            Long danceId,
            int place,
            Long id
    );

    boolean existsByCategoryTournamentIdAndJudgeIdAndPlaceAndIdNot(
            Long categoryId,
            Long judgeId,
            int place,
            Long id
    );

    boolean existsByCategoryTournamentIdAndParticipantTournamentId(Long categoryId, Long participantId);

    @Modifying
    @Query("""
          update FinalMark m
          set m.locked = true
          where m.categoryTournament.id = :categoryId
        """)
    void lockRound(Long categoryId);

    @Modifying
    @Query("""
          update FinalMark m
          set m.locked = true
          where m.categoryTournament.id = :categoryId
            and m.dance.id = :danceId
        """)
    void lockDanceRound(Long categoryId, Long danceId);

}
