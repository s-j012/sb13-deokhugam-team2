package com.deokhugam.user.service;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.user.dto.request.UserLoginRequest;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.request.UserUpdateRequest;
import com.deokhugam.user.dto.response.UserDto;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.exception.UserException;
import com.deokhugam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 - 성공")
    void register_success() {

        UserRegisterRequest request = new UserRegisterRequest("test@test.com", "nickname", "password123!");
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

        User user = User.create(request.email(), request.nickname(), "encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        UserDto response = userService.register(request);

        assertNotNull(response);
        assertEquals("test@test.com", response.email());
    }

    @Test
    @DisplayName("회원가입 - 실패 (이메일 중복)")
    void register_fail_duplicateEmail() {
        UserRegisterRequest request = new UserRegisterRequest("test@test.com", "nickname", "password123!");
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        UserException exception = assertThrows(UserException.class, () -> userService.register(request));
        assertEquals(ErrorCode.EMAIL_DUPLICATION, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        //given
        UserLoginRequest request = new UserLoginRequest("test@test.com", "password123!");
        User user = User.create("test@test.com", "nickname", "encodedPassword");

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);

        // when
        UserDto response = userService.login(request);

        // then
        assertNotNull(response);
        assertEquals("test@test.com", response.email());
    }

    @Test
    @DisplayName("사용자 조회 - 성공")
    void getUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@test.com", "nickname", "encodedPassword");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when
        UserDto response = userService.getUser(userId);

        // then
        assertNotNull(response);
        assertEquals("nickname", response.nickname());
    }

    @Test
    @DisplayName("사용자 수정 - 성공")
    void update_success() {
        // given
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        User user = User.create("test@test.com", "oldNickname", "password");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when
        UserDto response = userService.update(userId, request);

        // then
        assertEquals("newNickname", response.nickname());
        assertEquals("newNickname", user.getNickname());
    }

    @Test
    @DisplayName("사용자 수정 - 실패 (사용자 없음)")
    void update_fail_userNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        // when & then
        UserException exception = assertThrows(UserException.class, () -> userService.update(userId, request));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("소프트 삭제 - 성공")
    void softDelete_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@test.com", "nickname", "password");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when
        userService.softDelete(userId);

        // then
        assertNotNull(user.getDeletedAt());
    }

    @Test
    @DisplayName("하드 삭제 - 성공")
    void hardDelete_success() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        userService.hardDelete(userId);

        // then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("하드 삭제 - 실패 (사용자 없음)")
    void hardDelete_fail_userNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        UserException exception = assertThrows(UserException.class, () -> userService.hardDelete(userId));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("소프트 삭제 복구 스케줄러 - 성공")
    void restoreSoftDeletedUsers_success() {
        // given
        User user = User.create("test@test.com", "nickname", "password");
        user.softDelete();
        given(userRepository.findSoftDeletedBefore(any(LocalDateTime.class))).willReturn(List.of(user));

        // when
        userService.restoreSoftDeletedUsers();

        // then
        assertNull(user.getDeletedAt()); // 삭제 일자가 null로 초기화되었는지 확인
    }

}