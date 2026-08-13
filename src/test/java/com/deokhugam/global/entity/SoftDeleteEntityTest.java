package com.deokhugam.global.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SoftDeleteEntityTest {

    private static class TestEntity extends SoftDeleteEntity {
    }

    @Test
    void 논리_삭제하면_삭제_시간이_기록된다() {
        // given
        TestEntity entity = new TestEntity();

        // when
        entity.softDelete();

        // then
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    void 삭제되지_않은_엔티티는_삭제_상태가_아니다() {
        // given
        TestEntity entity = new TestEntity();

        // when
        boolean deleted = entity.isDeleted();

        // then
        assertThat(deleted).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
    }

    @Test
    void 삭제된_엔티티를_복구하면_삭제_시간이_초기화된다() {
        // given
        TestEntity entity = new TestEntity();
        entity.softDelete();

        // when
        entity.restore();

        // then
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.isDeleted()).isFalse();
    }
}