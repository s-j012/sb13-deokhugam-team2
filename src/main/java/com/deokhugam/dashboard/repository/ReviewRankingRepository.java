package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.ReviewRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRankingRepository extends JpaRepository<ReviewRanking, UUID> {
  @Query("SELECT MAX(rr.baseDate) FROM ReviewRanking rr WHERE rr.periodType = :periodType")
  Optional<LocalDate> findLatestBaseDate(@Param("periodType") PeriodType periodType);

  long countByPeriodTypeAndBaseDate(PeriodType periodType, LocalDate baseDate);

  List<ReviewRanking> findByPeriodTypeAndBaseDateAndRankingGreaterThanOrderByRankingAsc(
      PeriodType periodType, LocalDate baseDate, long ranking, Pageable pageable);

  List<ReviewRanking> findByPeriodTypeAndBaseDateAndRankingLessThanOrderByRankingDesc(
      PeriodType periodType, LocalDate baseDate, long ranking, Pageable pageable);
}