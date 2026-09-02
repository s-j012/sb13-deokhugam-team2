package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.UserRanking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRankingRepository extends JpaRepository<UserRanking, UUID> {

  @Query("SELECT MAX(ur.baseDate) FROM UserRanking ur WHERE ur.periodType = :periodType")
  Optional<LocalDate> findLatestBaseDate(@Param("periodType") PeriodType periodType);

  long countByPeriodTypeAndBaseDate(PeriodType periodType, LocalDate baseDate);

  @Query("""
      SELECT ur
      FROM UserRanking ur
      WHERE ur.periodType = :periodType
        AND ur.baseDate = :baseDate
        AND (
          :cursorRanking IS NULL
          OR ur.ranking > :cursorRanking
          OR (ur.ranking = :cursorRanking AND ur.createdAt > :after)
        )
      ORDER BY ur.ranking ASC, ur.createdAt ASC
      """)
  List<UserRanking> findRankingPageAsc(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRanking") Long cursorRanking,
      @Param("after") LocalDateTime after,
      Pageable pageable
  );

  @Query("""
      SELECT ur
      FROM UserRanking ur
      WHERE ur.periodType = :periodType
        AND ur.baseDate = :baseDate
        AND (
          :cursorRanking IS NULL
          OR ur.ranking < :cursorRanking
          OR (ur.ranking = :cursorRanking AND ur.createdAt < :after)
        )
      ORDER BY ur.ranking DESC, ur.createdAt DESC
      """)
  List<UserRanking> findRankingPageDesc(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRanking") Long cursorRanking,
      @Param("after") LocalDateTime after,
      Pageable pageable
  );

  @Modifying
  @Query("""
      DELETE FROM UserRanking ur
      WHERE ur.periodType = :periodType
        AND ur.baseDate = :baseDate
      """)
  void deleteSnapshot(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate
  );

  void deleteAllByUserId(UUID userId);
}
