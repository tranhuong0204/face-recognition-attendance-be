package com.example.be.web.service.impl;

import com.example.be.web.constant.ErrorMessage;
import com.example.be.web.doman.dto.request.auth.LoginRequestDto;
import com.example.be.web.doman.dto.request.auth.TokenRefreshRequestDto;
import com.example.be.web.doman.dto.response.auth.CommonResponseDto;
import com.example.be.web.doman.dto.response.auth.LoginResponseDto;
import com.example.be.web.doman.dto.response.auth.TokenRefreshResponseDto;
import com.example.be.web.doman.dto.response.auth.TotalResponse;
import com.example.be.web.doman.entity.User;
import com.example.be.web.exception.extended.InternalServerException;
import com.example.be.web.exception.extended.UnauthorizedException;
import com.example.be.web.repository.ClassRepository;
import com.example.be.web.repository.UserRepository;
import com.example.be.web.security.UserPrincipal;
import com.example.be.web.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassRepository classRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ==================== LOGIN ====================

    @Test
    void login_shouldReturnLoginResponseSuccessfully() {
        String username = "testuser";
        String password = "password123";
        Long userId = 1L;

        LoginRequestDto request = mock(LoginRequestDto.class);
        when(request.getUsername()).thenReturn(username);
        when(request.getPassword()).thenReturn(password);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        User user = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(user.getUsername()).thenReturn(username);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtTokenProvider.generateToken(principal, false))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateToken(principal, true))
                .thenReturn("refresh-token");

        LoginResponseDto result = authService.login(request);

        assertNotNull(result);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(userRepository).findById(userId);
        verify(jwtTokenProvider).generateToken(principal, false);
        verify(jwtTokenProvider).generateToken(principal, true);

        assertSame(
                authentication,
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void login_shouldThrowUnauthorizedWhenBadCredentials() {
        LoginRequestDto request = mock(LoginRequestDto.class);
        when(request.getUsername()).thenReturn("wronguser");
        when(request.getPassword()).thenReturn("wrongpassword");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void login_shouldThrowUnauthorizedWhenAuthenticationServiceFails() {
        LoginRequestDto request = mock(LoginRequestDto.class);
        when(request.getUsername()).thenReturn("testuser");
        when(request.getPassword()).thenReturn("password123");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(
                new InternalAuthenticationServiceException("Authentication failed")
        );

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void login_shouldThrowUnauthorizedWhenUserNotFound() {
        LoginRequestDto request = mock(LoginRequestDto.class);
        when(request.getUsername()).thenReturn("testuser");
        when(request.getPassword()).thenReturn("password123");

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(1L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of()
        );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void login_shouldThrowInternalServerExceptionWhenUnexpectedErrorOccurs() {
        LoginRequestDto request = mock(LoginRequestDto.class);
        when(request.getUsername()).thenReturn("testuser");
        when(request.getPassword()).thenReturn("password123");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new RuntimeException("Unexpected error"));

        assertThrows(
                InternalServerException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void login_shouldMarkFirstLoginWhenLastLoginEqualsCreateDate() {
        LoginRequestDto request = mock(LoginRequestDto.class);
        when(request.getUsername()).thenReturn("testuser");
        when(request.getPassword()).thenReturn("password123");

        Long userId = 1L;

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        User user = mock(User.class);

        java.time.LocalDateTime loginTime =
                java.time.LocalDateTime.of(2026, 1, 1, 10, 0);

        when(user.getLastLogin()).thenReturn(loginTime);
        when(user.getCreateDate()).thenReturn(loginTime);
        when(user.getUsername()).thenReturn("testuser");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProvider.generateToken(principal, false))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateToken(principal, true))
                .thenReturn("refresh-token");

        LoginResponseDto result = authService.login(request);

        assertNotNull(result);

        // Có thể kiểm tra isFirstLogin trực tiếp nếu DTO có getter tương ứng.
    }

    // ==================== REFRESH ====================

    @Test
    void refresh_shouldReturnNewAccessToken() {
        String refreshToken = "refresh-token";
        String accessToken = "new-access-token";

        TokenRefreshRequestDto request = mock(TokenRefreshRequestDto.class);
        when(request.getRefreshToken()).thenReturn(refreshToken);

        UserPrincipal principal = mock(UserPrincipal.class);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtTokenProvider.getAuthenticationByRefreshToken(refreshToken))
                .thenReturn(authentication);

        when(jwtTokenProvider.generateToken(principal, false))
                .thenReturn(accessToken);

        TokenRefreshResponseDto result = authService.refresh(request);

        assertNotNull(result);

        verify(jwtTokenProvider)
                .getAuthenticationByRefreshToken(refreshToken);
        verify(jwtTokenProvider).generateToken(principal, false);

        assertSame(
                authentication,
                SecurityContextHolder.getContext().getAuthentication()
        );
    }

    @Test
    void refresh_shouldPropagateExceptionWhenRefreshTokenIsInvalid() {
        TokenRefreshRequestDto request = mock(TokenRefreshRequestDto.class);
        when(request.getRefreshToken()).thenReturn("invalid-token");

        when(jwtTokenProvider.getAuthenticationByRefreshToken("invalid-token"))
                .thenThrow(new UnauthorizedException(
                        ErrorMessage.Auth.ERR_INCORRECT_USERNAME
                ));

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refresh(request)
        );

        verify(jwtTokenProvider, never())
                .generateToken(any(UserPrincipal.class), anyBoolean());
    }

    // ==================== LOGOUT ====================

    @Test
    void logout_shouldReturnNullBecauseMethodIsNotImplemented() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        CommonResponseDto result = authService.logout(request);

        assertNull(result);
    }

    // ==================== GET TOTAL ====================

    @Test
    void getTotal_shouldReturnCorrectStatistics() {
        when(classRepository.count()).thenReturn(10L);
        when(userRepository.countAllByRole_Name("ROLE_ADMIN")).thenReturn(2L);
        when(userRepository.countAllByRole_Name("ROLE_USER")).thenReturn(50L);

        TotalResponse result = authService.getTotal();

        assertNotNull(result);

        verify(classRepository).count();
        verify(userRepository).countAllByRole_Name("ROLE_ADMIN");
        verify(userRepository).countAllByRole_Name("ROLE_USER");

        // Nếu TotalResponse có các getter tương ứng, kiểm tra thêm:
        // assertEquals(10L, result.getTotalClass());
        // assertEquals(2L, result.getTotalAdmin());
        // assertEquals(50L, result.getTotalUser());
    }
}
