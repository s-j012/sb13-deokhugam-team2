package com.deokhugam.comment.repository;

import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl
        implements CommentRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Comment> findAllByCursor(
            CommentSearchRequest request
    ) {
        StringBuilder jpql = new StringBuilder(
                """
                SELECT c
                FROM Comment c
                WHERE c.deletedAt IS NULL
                  AND c.reviewId = :reviewId
                """
        );

        String direction =
                resolveDirection(request.direction());

        String operator =
                direction.equals("DESC") ? "<" : ">";

        boolean hasCursor =
                request.cursor() != null
                        && !request.cursor().isBlank();

        if (hasCursor) {
            jpql.append(" AND c.createdAt ")
                    .append(operator)
                    .append(" :cursor");
        }

        jpql.append(" ORDER BY c.createdAt ")
                .append(direction)
                .append(", c.id ")
                .append(direction);

        TypedQuery<Comment> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Comment.class
                );

        query.setParameter(
                "reviewId",
                request.reviewId()
        );

        if (hasCursor) {
            query.setParameter(
                    "cursor",
                    LocalDateTime.parse(request.cursor())
            );
        }

        query.setMaxResults(request.limit() + 1);

        return query.getResultList();
    }

    @Override
    public long countAll(
            CommentSearchRequest request
    ) {
        TypedQuery<Long> query =
                entityManager.createQuery(
                        """
                        SELECT COUNT(c)
                        FROM Comment c
                        WHERE c.deletedAt IS NULL
                          AND c.reviewId = :reviewId
                        """,
                        Long.class
                );

        query.setParameter(
                "reviewId",
                request.reviewId()
        );

        return query.getSingleResult();
    }

    private String resolveDirection(
            String direction
    ) {
        if ("ASC".equalsIgnoreCase(direction)) {
            return "ASC";
        }

        if ("DESC".equalsIgnoreCase(direction)) {
            return "DESC";
        }

        throw new IllegalArgumentException(
                "지원하지 않는 정렬 방향입니다: "
                        + direction
        );
    }
}