package com.monstersinc.stock101.user;

import com.monstersinc.stock101.auth.model.dto.LoginResponse;
import com.monstersinc.stock101.auth.model.mapper.AuthMapper;
import com.monstersinc.stock101.auth.model.service.AuthServiceImpl;
import com.monstersinc.stock101.auth.jwt.JwtTokenProvider;
import com.monstersinc.stock101.auth.jwt.JwtUtil;
import com.monstersinc.stock101.common.model.vo.CommonConstants;
import com.monstersinc.stock101.exception.message.GlobalExceptionMessage;
import com.monstersinc.stock101.user.model.dto.UpdateProfileRequestDto;
import com.monstersinc.stock101.user.model.dto.UserRegisterRequestDto;
import com.monstersinc.stock101.user.model.mapper.UserMapper;
import com.monstersinc.stock101.user.model.service.UserServiceImpl;
import com.monstersinc.stock101.user.model.vo.Role;
import com.monstersinc.stock101.user.model.vo.User;
import com.monstersinc.stock101.user.model.vo.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserserviceTest {

    // 사용자 및 인증 서비스 핵심 로직을 단위 테스트로 검증한다
    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<Object, Object> redisTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    class RegisterUserTests {
        // 회원가입 시나리오(신규 가입, 중복 처리, 재가입 정책)를 검증

        @Test
        @DisplayName("회원가입은 신규 이메일에 대해 암호화된 비밀번호와 기본 권한을 저장한다")
        void registerUserCreatesNewAccount() {
            // 신규 이메일 가입 시 암호화 및 기본 권한이 정상 설정되는지 검증
            String email = "user@example.com";
            String rawPassword = "plainPw123";
            UserRegisterRequestDto request = new UserRegisterRequestDto(email, "사용자", rawPassword);

            when(userMapper.findByEmail(email)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPw");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            User registered = userService.registerUser(request);

            verify(userMapper).insertUser(userCaptor.capture());
            User saved = userCaptor.getValue();

            assertThat(saved.getEmail()).isEqualTo(email);
            assertThat(saved.getPassword()).isEqualTo("encodedPw");
            assertThat(saved.getName()).isEqualTo("사용자");
            assertThat(saved.getRole()).isEqualTo(Role.USER);
            assertThat(saved.getTierCode()).isEqualTo("BRONZE");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(registered).isSameAs(saved);
            verify(passwordEncoder).encode(rawPassword);
            verify(userMapper, never()).updateEmail(anyLong(), anyString());
        }

        @Test
        @DisplayName("활성 계정 이메일로 회원가입을 시도하면 DUPLICATE_EMAIL 예외가 발생한다")
        void registerUserFailsForActiveDuplicate() {
            // 이미 사용 중인 이메일은 예외를 던져야 한다
            String email = "dup@example.com";
            UserRegisterRequestDto request = new UserRegisterRequestDto(email, "사용자", "plainPw123");
            User existing = User.builder()
                    .userId(11L)
                    .email(email)
                    .role(Role.USER)
                    .deletedAt(null)
                    .build();

            when(userMapper.findByEmail(email)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userService.registerUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.DUPLICATE_EMAIL.name());

            verify(userMapper, never()).insertUser(any(User.class));
            verify(userMapper, never()).updateEmail(anyLong(), anyString());
        }

        @Test
        @DisplayName("탈퇴 후 14일이 지나지 않은 계정 재가입은 DUPLICATE_EMAIL 예외를 던진다")
        void registerUserFailsWithinGracePeriod() {
            // 탈퇴 유예기간 내에는 동일 이메일 재가입을 허용하지 않는다
            String email = "rejoin@example.com";
            UserRegisterRequestDto request = new UserRegisterRequestDto(email, "사용자", "plainPw123");
            User existing = User.builder()
                    .userId(21L)
                    .email(email)
                    .role(Role.USER)
                    .deletedAt(LocalDateTime.now().minusDays(5))
                    .build();

            when(userMapper.findByEmail(email)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> userService.registerUser(request))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.DUPLICATE_EMAIL.name());

            verify(userMapper, never()).insertUser(any(User.class));
            verify(userMapper, never()).updateEmail(anyLong(), anyString());
        }

        @Test
        @DisplayName("탈퇴 후 14일이 지난 계정은 이메일을 마스킹하고 새 계정을 만든다")
        void registerUserMarksOldEmailAndCreatesNewAccount() {
            // 유예기간이 지난 탈퇴 계정은 이메일을 마스킹한 뒤 새 레코드로 등록되어야 한다
            String email = "longago@example.com";
            UserRegisterRequestDto request = new UserRegisterRequestDto(email, "새 사용자", "plainPw123");
            LocalDateTime deletedAt = LocalDateTime.now().minusDays(CommonConstants.USER_DELETION_EXPIRE_DAYS + 1L);
            User existing = User.builder()
                    .userId(31L)
                    .email(email)
                    .role(Role.USER)
                    .deletedAt(deletedAt)
                    .build();

            when(userMapper.findByEmail(email)).thenReturn(Optional.of(existing));
            when(passwordEncoder.encode("plainPw123")).thenReturn("encodedPw");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            userService.registerUser(request);

            String expectedMarkedEmail = email + "_DEL_" + deletedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            verify(userMapper).updateEmail(existing.getUserId(), expectedMarkedEmail);
            verify(userMapper).insertUser(userCaptor.capture());

            User saved = userCaptor.getValue();
            assertThat(saved.getEmail()).isEqualTo(email);
            assertThat(saved.getPassword()).isEqualTo("encodedPw");
            assertThat(saved.getRole()).isEqualTo(Role.USER);
        }
    }

    @Nested
    class ProfileTests {
        // 프로필 수정 및 탈퇴 처리 등 사용자 상태 변경 로직을 확인

        @Test
        @DisplayName("프로필 수정은 전달된 필드만 갱신하고 매퍼를 호출한다")
        void updateProfileUpdatesFields() {
            // 변경 요청이 있는 필드만 업데이트되는지 확인
            Long userId = 100L;
            User existing = User.builder()
                    .userId(userId)
                    .name("기존 이름")
                    .statusMessage("기존 상태")
                    .role(Role.USER)
                    .build();

            UpdateProfileRequestDto request = new UpdateProfileRequestDto("새 이름", "새 상태");

            when(userMapper.findByUserId(userId)).thenReturn(Optional.of(existing));

            User updated = userService.updateProfile(userId, request);

            assertThat(updated.getName()).isEqualTo("새 이름");
            assertThat(updated.getStatusMessage()).isEqualTo("새 상태");
            verify(userMapper).updateUserProfile(existing);
        }

        @Test
        @DisplayName("soft delete는 deletedAt을 현재 시각으로 설정한다")
        void softDeleteMarksUser() {
            // 소프트 삭제 시점이 현재 시간으로 기록되는지 검증
            Long userId = 200L;
            User existing = User.builder()
                    .userId(userId)
                    .role(Role.USER)
                    .build();

            when(userMapper.findByUserId(userId)).thenReturn(Optional.of(existing));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            userService.softDeleteUser(userId);

            verify(userMapper).updateUserProfile(captor.capture());
            assertThat(captor.getValue().getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이메일 존재 여부 조회는 매퍼 결과를 그대로 전달한다")
        void checkEmailExistsDelegatesToMapper() {
            // 이메일 존재 여부는 Mapper 결과와 일치해야 한다
            String email = "check@example.com";
            when(userMapper.existsByEmail(email)).thenReturn(true);

            assertThat(userService.checkEmailExists(email)).isTrue();
            verify(userMapper).existsByEmail(email);
        }
    }

    @Nested
    class RetrievalTests {
        // 조회 관련 메서드들이 Optional/null을 안전하게 처리하는지 검증
        @Test
        @DisplayName("이메일로 사용자 조회는 Optional을 적절히 처리한다")
        void getUserByEmailHandlesOptional() {
            // Optional이 존재할 때와 없을 때 모두 기대하는 결과를 돌려주는지 확인
            String email = "find@example.com";
            User user = User.builder().userId(1L).email(email).role(Role.USER).build();
            when(userMapper.findByEmail(email)).thenReturn(Optional.of(user));

            assertThat(userService.getUserByEmail(email)).isEqualTo(user);

            when(userMapper.findByEmail(email)).thenReturn(Optional.empty());
            assertThat(userService.getUserByEmail(email)).isNull();
        }

        @Test
        @DisplayName("userId로 사용자 프로필을 조회한다")
        void getUserProfileByIdDelegatesToMapper() {
            // 프로필 조회는 Mapper 위임 여부만 확인하면 충분하다
            Long userId = 55L;
            UserProfile profile = UserProfile.builder().userId(userId).build();
            when(userMapper.getUserProfileById(userId)).thenReturn(profile);

            assertThat(userService.getUserProfileById(userId)).isEqualTo(profile);
            verify(userMapper).getUserProfileById(userId);
        }
    }

    @Nested
    class AuthServiceTests {
        // 인증 서비스의 로그인·로그아웃·토큰 재발급 흐름을 통합적으로 확인

        private User baseUser() {
            // 테스트에서 반복 사용할 기본 사용자 객체
            return User.builder()
                    .userId(999L)
                    .email("auth@example.com")
                    .password("encodedPw")
                    .name("로그인 사용자")
                    .role(Role.USER)
                    .tierCode("GOLD")
                    .statusMessage("상태 메시지")
                    .build();
        }

        @Test
        @DisplayName("활성 계정 로그인 시 액세스 토큰과 사용자 정보를 반환한다")
        void loginSuccessForActiveUser() {
            // 정상 로그인 시 토큰 정보와 사용자 메타데이터가 채워지는지 확인
            User user = baseUser();
            String email = user.getEmail();
            String rawPassword = "plainPw123";
            String accessToken = "access-token";
            long issuedAt = 1000L;
            long expiresAt = 2000L;
            List<String> roles = List.of(Role.USER.name());

            when(authMapper.selectUserByEmail(email)).thenReturn(user);
            when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(eq(user.getUserId()), eq(roles))).thenReturn(accessToken);
            when(jwtUtil.getIssuedAt(accessToken)).thenReturn(issuedAt);
            when(jwtUtil.getExpiration(accessToken)).thenReturn(expiresAt);

            LoginResponse response = authService.login(email, rawPassword);

            assertThat(response.getAccessToken()).isEqualTo(accessToken);
            assertThat(response.getUserId()).isEqualTo(user.getUserId());
            assertThat(response.getRoles()).containsExactlyElementsOf(roles);
            assertThat(response.getTierCode()).isEqualTo(user.getTierCode());
            assertThat(response.getUserName()).isEqualTo(user.getUsername());
            assertThat(response.getStatusMessage()).isEqualTo(user.getStatusMessage());
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getIssuedAt()).isEqualTo(issuedAt);
            assertThat(response.getExpiresAt()).isEqualTo(expiresAt);

            verify(authMapper).updateLastLogin(eq(user.getUserId()), any(LocalDateTime.class));
            verify(authMapper, never()).cancelDeleteUser(anyLong());
        }

        @Test
        @DisplayName("탈퇴 후 14일 이내 로그인하면 탈퇴가 취소된다")
        void loginCancelsDeletionWithinGracePeriod() {
            // 탈퇴 유예기간 내 로그인 시 삭제 플래그가 해제되는지 검증
            User user = baseUser();
            user.setDeletedAt(LocalDateTime.now().minusDays(CommonConstants.USER_DELETION_EXPIRE_DAYS - 1L));
            String email = user.getEmail();
            String rawPassword = "plainPw123";
            String accessToken = "restored-access";
            List<String> roles = List.of(Role.USER.name());

            when(authMapper.selectUserByEmail(email)).thenReturn(user);
            when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);
            when(jwtTokenProvider.createAccessToken(eq(user.getUserId()), eq(roles))).thenReturn(accessToken);
            when(jwtUtil.getIssuedAt(accessToken)).thenReturn(1L);
            when(jwtUtil.getExpiration(accessToken)).thenReturn(2L);

            authService.login(email, rawPassword);

            assertThat(user.getDeletedAt()).isNull();
            verify(authMapper).cancelDeleteUser(user.getUserId());
            verify(authMapper).updateLastLogin(eq(user.getUserId()), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("탈퇴 후 14일이 지난 계정은 로그인할 수 없다")
        void loginFailsAfterGracePeriod() {
            // 유예기간 이후 로그인은 실패하고 예외를 던져야 한다
            User user = baseUser();
            user.setDeletedAt(LocalDateTime.now().minusDays(CommonConstants.USER_DELETION_EXPIRE_DAYS + 1L));
            String email = user.getEmail();
            String rawPassword = "plainPw123";

            when(authMapper.selectUserByEmail(email)).thenReturn(user);
            when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);

            assertThatThrownBy(() -> authService.login(email, rawPassword))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.USER_NOT_FOUND.name());

            verify(authMapper, never()).updateLastLogin(anyLong(), any(LocalDateTime.class));
            verify(jwtTokenProvider, never()).createAccessToken(anyLong(), anyList());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 로그인 시 USER_NOT_FOUND 예외가 발생한다")
        void loginFailsWhenUserMissing() {
            // 사용자 조회에 실패하면 동일한 예외를 반환해야 한다
            String email = "missing@example.com";

            assertThatThrownBy(() -> authService.login(email, "plainPw123"))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.USER_NOT_FOUND.name());

            verify(authMapper).selectUserByEmail(email);
        }

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
        void loginFailsOnWrongPassword() {
            // 비밀번호가 틀릴 경우 USER_NOT_FOUND 예외를 던지는지 검사
            User user = baseUser();
            String email = user.getEmail();

            when(authMapper.selectUserByEmail(email)).thenReturn(user);
            when(passwordEncoder.matches("wrongPw", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(email, "wrongPw"))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.USER_NOT_FOUND.name());
        }

        @Test
        @DisplayName("리프레시 토큰 생성은 공급자 구현을 호출한다")
        void createRefreshTokenDelegates() {
            // 리프레시 토큰 생성 요청이 JwtTokenProvider로 위임되는지 확인
            when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");

            assertThat(authService.createRefreshToken(1L)).isEqualTo("refresh-token");
            verify(jwtTokenProvider).createRefreshToken(1L);
        }

        @Test
        @DisplayName("로그아웃 시 액세스 토큰을 블랙리스트에 추가하고 리프레시 토큰을 삭제한다")
        void logoutBlacklistsAccessToken() {
            // 정상 로그아웃 시 블랙리스트 등록과 리프레시 토큰 제거가 호출되는지 확인
            String bearer = "Bearer access";
            when(jwtTokenProvider.resolveToken(bearer)).thenReturn("access");

            authService.logout(bearer);

            verify(jwtTokenProvider).addBlacklist("access");
            verify(jwtTokenProvider).deleteRefreshToken("access");
        }

        @Test
        @DisplayName("토큰이 비어있으면 로그아웃에서 예외를 던진다")
        void logoutFailsWhenTokenMissing() {
            // 헤더에 유효한 토큰이 없으면 예외가 발생해야 한다
            String bearer = "Bearer invalid";
            when(jwtTokenProvider.resolveToken(bearer)).thenReturn(null);

            assertThatThrownBy(() -> authService.logout(bearer))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.UNAUTHORIZED_TOKEN.name());

            verify(jwtTokenProvider, never()).addBlacklist(anyString());
        }

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 갱신한다")
        void refreshAccessTokenSuccess() {
            // 리프레시 토큰이 유효할 때 새 액세스 토큰이 발급되는지 확인
            User user = baseUser();
            String refreshToken = "refresh-token";
            String newAccess = "new-access";
            List<String> roles = List.of(Role.USER.name());

            when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.isValidRefreshToken(refreshToken)).thenReturn(true);
            when(jwtUtil.getUserId(refreshToken)).thenReturn(String.valueOf(user.getUserId()));
            when(authMapper.selectUserByUserId(user.getUserId())).thenReturn(user);
            when(jwtTokenProvider.createAccessToken(eq(user.getUserId()), eq(roles))).thenReturn(newAccess);
            when(jwtUtil.getIssuedAt(newAccess)).thenReturn(10L);
            when(jwtUtil.getExpiration(newAccess)).thenReturn(20L);

            LoginResponse response = authService.refreshAccessToken(refreshToken);

            assertThat(response.getAccessToken()).isEqualTo(newAccess);
            assertThat(response.getUserId()).isEqualTo(user.getUserId());
            assertThat(response.getRoles()).containsExactlyElementsOf(roles);
            verify(jwtTokenProvider).isValidRefreshToken(refreshToken);
            verify(authMapper).selectUserByUserId(user.getUserId());
        }

        @Test
        @DisplayName("리프레시 토큰 검증에 실패하면 예외를 던진다")
        void refreshAccessTokenFailsWhenInvalid() {
            // 토큰 자체가 유효하지 않으면 즉시 예외를 던져야 한다
            String refreshToken = "bad-refresh";
            when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

            assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.UNAUTHORIZED_TOKEN.name());

            verify(jwtTokenProvider, never()).isValidRefreshToken(anyString());
        }

        @Test
        @DisplayName("리프레시 토큰이 저장된 토큰과 일치하지 않으면 예외를 던진다")
        void refreshAccessTokenFailsWhenMismatch() {
            // 저장된 리프레시 토큰과 다르면 재발급을 거부해야 한다
            String refreshToken = "mismatch-refresh";
            when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
            when(jwtTokenProvider.isValidRefreshToken(refreshToken)).thenReturn(false);

            assertThatThrownBy(() -> authService.refreshAccessToken(refreshToken))
                    .isInstanceOf(RuntimeException.class)
                    .extracting("type")
                    .isEqualTo(GlobalExceptionMessage.UNAUTHORIZED_TOKEN.name());

            verify(authMapper, never()).selectUserByUserId(anyLong());
        }
    }
}
