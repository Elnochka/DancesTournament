package com.dances.repository;

import com.dances.entity.CoupleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CoupleHistoryRepository extends JpaRepository<CoupleHistory, Long> {

    @Query("select h from CoupleHistory h where h.couple.id = :cid order by h.startDate desc")
    List<CoupleHistory> findByCoupleId(@Param("cid") Long coupleId);

    @Modifying
    @Query("update CoupleHistory h set h.endDate = :end where h.couple.id = :cid and h.endDate is null")
    int closeActiveByCoupleId(@Param("cid") Long coupleId, @Param("end") LocalDate endDate);
}
