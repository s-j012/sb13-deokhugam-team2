package com.deokhugam.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.user.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("여러 사용자 조회 시 논리 삭제된 사용자를 제외한다")
  void findActiveUsers() {
    User activeUser = userRepository.save(
        createUser("active")
    );

    User deletedUser = createUser("deleted");
    deletedUser.softDelete();
    deletedUser = userRepository.save(deletedUser);

    userRepository.flush();

    List<User> result =
        userRepository.findAllByIdInAndDeletedAtIsNull(
            List.of(
                activeUser.getId(),
                deletedUser.getId()
            )
        );

    assertThat(result)
        .extracting(User::getId)
        .containsExactly(activeUser.getId());
  }

  private User createUser(String prefix) {
    return User.create(
        prefix + "-" + UUID.randomUUID() + "@example.com",
        prefix + "User",
        "encodedPassword"
    );
  }
}