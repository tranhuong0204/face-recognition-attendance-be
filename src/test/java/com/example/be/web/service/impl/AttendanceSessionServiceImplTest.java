package com.example.be.web.service.impl;

import com.example.be.web.doman.dto.request.attendance.AttendanceSessionRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.doman.entity.AttendanceSession;
import com.example.be.web.doman.entity.ClassRoom;
import com.example.be.web.doman.entity.Location;
import com.example.be.web.doman.entity.User;
import com.example.be.web.doman.mapper.AttendanceSessionMapper;
import com.example.be.web.doman.model.AttendanceStatus;
import com.example.be.web.doman.model.RecordStatus;
import com.example.be.web.exception.extended.InternalServerException;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.AttendanceSessionRepository;
import com.example.be.web.repository.UserRepository;
import com.example.be.web.repository.LocationRepository;
import com.example.be.web.repository.ClassRepository;
import com.example.be.web.repository.ClassRegistrationRepository;
import com.example.be.web.security.UserPrincipal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceSessionServiceImplTest {
    @org.mockito.Mock
    private AttendanceSessionRepository sessionRepository;

    @org.mockito.Mock
    private AttendanceSessionMapper mapper;

    @org.mockito.Mock
    private UserRepository userRepository;

    @org.mockito.Mock
    private LocationRepository locationRepository;

    @org.mockito.Mock
    private ClassRepository classRepository;

    @org.mockito.Mock
    private ClassRegistrationRepository classRegistrationRepository;

    @org.mockito.InjectMocks
    private AttendanceSessionServiceImpl service;

    private void mockCurrentUser(Long userId) {
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(userId);

        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void createSession_shouldCreateSessionSuccessfully() {
        Long classId = 1L;
        Long locationId = 2L;
        Long userId = 10L;

        mockCurrentUser(userId);

        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);
        when(request.getClassId()).thenReturn(classId);
        when(request.getLocationId()).thenReturn(locationId);
        when(request.getStatus()).thenReturn(null);

        AttendanceSession session = mock(AttendanceSession.class);
        ClassRoom classRoom = mock(ClassRoom.class);
        Location location = mock(Location.class);
        User creator = mock(User.class);
        AttendanceSessionResponseDto expected =
                mock(AttendanceSessionResponseDto.class);

        when(mapper.toEntity(request)).thenReturn(session);
        when(classRepository.findById(classId))
                .thenReturn(Optional.of(classRoom));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(creator));
        when(locationRepository.findById(locationId))
                .thenReturn(Optional.of(location));
        when(sessionRepository.save(session)).thenReturn(session);
        when(mapper.toResponse(session)).thenReturn(expected);

        AttendanceSessionResponseDto result = service.createSession(request);

        assertSame(expected, result);

        verify(session).setClassRoom(classRoom);
        verify(session).setCreatedByUser(creator);
        verify(session).setLocation(location);
        verify(session).setStatus(AttendanceStatus.OPEN);
        verify(sessionRepository).save(session);
        verify(mapper).toResponse(session);
    }

    @Test
    void createSession_shouldThrowExceptionWhenLocationIdIsNull() {
        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);

        when(request.getLocationId()).thenReturn(null);

        assertThrows(
                NotFoundException.class,
                () -> service.createSession(request)
        );

        verifyNoInteractions(mapper, classRepository, userRepository,
                locationRepository, sessionRepository);
    }

    @Test
    void createSession_shouldThrowExceptionWhenClassNotFound() {
        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);

        when(request.getLocationId()).thenReturn(2L);
        when(request.getClassId()).thenReturn(999L);
        when(mapper.toEntity(request)).thenReturn(mock(AttendanceSession.class));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.createSession(request)
        );

        verifyNoInteractions(sessionRepository);
    }

    @Test
    void createSession_shouldThrowExceptionWhenCreatorNotFound() {
        Long userId = 10L;
        mockCurrentUser(userId);

        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);
        when(request.getLocationId()).thenReturn(2L);
        when(request.getClassId()).thenReturn(1L);

        when(mapper.toEntity(request)).thenReturn(mock(AttendanceSession.class));
        when(classRepository.findById(1L)).thenReturn(Optional.of(mock(ClassRoom.class)));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.createSession(request)
        );

        verifyNoInteractions(locationRepository, sessionRepository);
    }

    @Test
    void createSession_shouldThrowExceptionWhenLocationNotFound() {
        Long userId = 10L;
        mockCurrentUser(userId);

        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);
        when(request.getLocationId()).thenReturn(999L);
        when(request.getClassId()).thenReturn(1L);

        when(mapper.toEntity(request)).thenReturn(mock(AttendanceSession.class));
        when(classRepository.findById(1L)).thenReturn(Optional.of(mock(ClassRoom.class)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock(User.class)));
        when(locationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.createSession(request)
        );

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void updateSession_shouldUpdateSuccessfully() {
        Long sessionId = 1L;
        Long classId = 2L;
        Long locationId = 3L;

        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);
        when(request.getClassId()).thenReturn(classId);
        when(request.getLocationId()).thenReturn(locationId);

        AttendanceSession existing = mock(AttendanceSession.class);
        ClassRoom classRoom = mock(ClassRoom.class);
        Location location = mock(Location.class);
        AttendanceSessionResponseDto expected =
                mock(AttendanceSessionResponseDto.class);

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(existing));
        when(classRepository.findById(classId))
                .thenReturn(Optional.of(classRoom));
        when(locationRepository.findById(locationId))
                .thenReturn(Optional.of(location));
        when(sessionRepository.save(existing)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(expected);

        AttendanceSessionResponseDto result =
                service.updateSession(sessionId, request);

        assertSame(expected, result);

        verify(mapper).updateEntityFromDto(request, existing);
        verify(existing).setClassRoom(classRoom);
        verify(existing).setLocation(location);
        verify(sessionRepository).save(existing);
    }

    @Test
    void updateSession_shouldThrowExceptionWhenSessionNotFound() {
        Long sessionId = 999L;

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.updateSession(
                        sessionId, mock(AttendanceSessionRequestDto.class)
                )
        );

        verifyNoInteractions(mapper);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void updateSession_shouldThrowExceptionWhenClassNotFound() {
        Long sessionId = 1L;
        Long classId = 999L;

        AttendanceSession existing = mock(AttendanceSession.class);
        AttendanceSessionRequestDto request =
                mock(AttendanceSessionRequestDto.class);

        when(request.getClassId()).thenReturn(classId);
        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(existing));
        when(classRepository.findById(classId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.updateSession(sessionId, request)
        );

        verify(sessionRepository, never()).save(any());
    }

    @Test
    void deleteSession_shouldDeleteSuccessfully() {
        Long sessionId = 1L;
        AttendanceSession session = mock(AttendanceSession.class);

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        assertDoesNotThrow(() -> service.deleteSession(sessionId));

        verify(sessionRepository).delete(session);
    }

    @Test
    void deleteSession_shouldThrowExceptionWhenSessionNotFound() {
        Long sessionId = 999L;

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.deleteSession(sessionId)
        );

        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void countSessionsByClassId_shouldReturnCount() {
        Long classId = 1L;

        when(classRepository.findById(classId))
                .thenReturn(Optional.of(mock(ClassRoom.class)));
        when(sessionRepository.countByClassRoom_ClassId(classId))
                .thenReturn(5L);

        long result = service.countSessionsByClassId(classId);

        assertEquals(5L, result);
        verify(sessionRepository).countByClassRoom_ClassId(classId);
    }

    @Test
    void countSessionsByClassId_shouldThrowExceptionWhenClassNotFound() {
        Long classId = 999L;

        when(classRepository.findById(classId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.countSessionsByClassId(classId)
        );

        verify(sessionRepository, never())
                .countByClassRoom_ClassId(any());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}