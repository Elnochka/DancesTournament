package com.dances.repository;

import com.dances.entity.ParticipantTrainerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ParticipantTrainerHistoryRepository extends JpaRepository<ParticipantTrainerHistory, Long> {

    @Query("select h from ParticipantTrainerHistory h where h.participant.id = :pid order by h.startDate desc")
    List<ParticipantTrainerHistory> findByParticipantId(@Param("pid") Long participantId);

    @Modifying
    @Query("update ParticipantTrainerHistory h set h.endDate = :end where h.participant.id = :pid and h.endDate is null")
    int closeActiveByParticipantId(@Param("pid") Long participantId, @Param("end") LocalDate endDate);
}
