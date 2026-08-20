package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.UserRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRankingRepository extends JpaRepository<UserRanking, UUID> {
  @Query("SELECT MAX(ur.baseDate) FROM UserRanking ur WHERE ur.periodType = :periodType")
  Optional<LocalDate> findLatestBaseDate(@Param("periodType") PeriodType periodType);

  long countByPeriodTypeAndBaseDate(PeriodType periodType, LocalDate baseDate);

  List<UserRanking> findByPeriodTypeAndBaseDateAndRankingGreaterThanOrderByRankingAsc(
      PeriodType periodType, LocalDate baseDate, long ranking, Pageable pageable);

  List<UserRanking> findByPeriodTypeAndBaseDateAndRankingLessThanOrderByRankingDesc(
      PeriodType periodType, LocalDate baseDate, long ranking, Pageable pageable);
}