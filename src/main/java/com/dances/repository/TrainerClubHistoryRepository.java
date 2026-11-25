package com.dances.repository;

import com.dances.entity.TrainerClubHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainerClubHistoryRepository extends JpaRepository<TrainerClubHistory, Long> {

    @Query("select h from TrainerClubHistory h where h.trainer.id = :pid order by h.startDate desc")
    List<TrainerClubHistory> findByTrainerId(@Param("pid") Long trainerId);

    @Modifying
    @Query("update TrainerClubHistory h set h.endDate = :end where h.trainer.id = :pid and h.endDate is null")
    int closeActiveByTrainerId(@Param("pid") Long trainerId, @Param("end") LocalDate endDate);
}
