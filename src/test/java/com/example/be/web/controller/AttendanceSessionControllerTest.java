package com.example.be.web.controller;

import com.example.be.web.doman.dto.request.attendance.AttendanceSessionRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.service.AttendanceSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceSessionControllerTest {

    @Mock
    private AttendanceSessionService sessionService;

    @InjectMocks
    private AttendanceSessionController controller;

    @Test
    void createSession_shouldReturnCreatedSession() {
        AttendanceSessionRequestDto request = new AttendanceSessionRequestDto();
        AttendanceSessionResponseDto response =
                new AttendanceSessionResponseDto();

        when(sessionService.createSession(request)).thenReturn(response);

        ResponseEntity<AttendanceSessionResponseDto> result =
                controller.createSession(request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(sessionService).createSession(request);
    }

    @Test
    void updateSession_shouldReturnUpdatedSession() {
        Long id = 1L;
        AttendanceSessionRequestDto request =
                new AttendanceSessionRequestDto();
        AttendanceSessionResponseDto response =
                new AttendanceSessionResponseDto();

        when(sessionService.updateSession(id, request))
                .thenReturn(response);

        ResponseEntity<AttendanceSessionResponseDto> result =
                controller.updateSession(id, request);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(sessionService).updateSession(id, request);
    }

    @Test
    void deleteSession_shouldReturnNoContent() {
        Long id = 1L;

        ResponseEntity<Void> result = controller.deleteSession(id);

        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());
        verify(sessionService).deleteSession(id);
    }

    @Test
    void getSessionById_shouldReturnSession() {
        Long id = 1L;
        AttendanceSessionResponseDto response =
                new AttendanceSessionResponseDto();

        when(sessionService.getSessionById(id)).thenReturn(response);

        ResponseEntity<AttendanceSessionResponseDto> result =
                controller.getSessionById(id);

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(sessionService).getSessionById(id);
    }

    @Test
    void getAllSessions_shouldReturnSessionList() {
        List<AttendanceSessionResponseDto> sessions = List.of(
                new AttendanceSessionResponseDto(),
                new AttendanceSessionResponseDto()
        );

        when(sessionService.getAllSessions()).thenReturn(sessions);

        ResponseEntity<List<AttendanceSessionResponseDto>> result =
                controller.getAllSessions();

        assertEquals(200, result.getStatusCode().value());
        assertSame(sessions, result.getBody());
        assertEquals(2, result.getBody().size());
        verify(sessionService).getAllSessions();
    }

    @Test
    void getAllSessions_shouldReturnEmptyListWhenNoSessions() {
        when(sessionService.getAllSessions()).thenReturn(List.of());

        ResponseEntity<List<AttendanceSessionResponseDto>> result =
                controller.getAllSessions();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(sessionService).getAllSessions();
    }

    @Test
    void countPresentStudentsInSession_shouldReturnCount() {
        Long sessionId = 1L;
        long expectedCount = 25L;

        when(sessionService.countPresentStudentsInSession(sessionId))
                .thenReturn(expectedCount);

        ResponseEntity<Long> result =
                controller.countPresentStudentsInSession(sessionId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(expectedCount, result.getBody());
        verify(sessionService).countPresentStudentsInSession(sessionId);
    }

    @Test
    void getOpenSessionsForStudent_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);

        List<AttendanceSessionResponseDto> content = List.of(
                new AttendanceSessionResponseDto(),
                new AttendanceSessionResponseDto()
        );

        Page<AttendanceSessionResponseDto> page =
                new PageImpl<>(content, pageable, content.size());

        when(sessionService.getOpenSessionsForStudent(pageable))
                .thenReturn(page);

        ResponseEntity<Page<AttendanceSessionResponseDto>> result =
                controller.getOpenSessionsForStudent(pageable);

        assertEquals(200, result.getStatusCode().value());
        assertSame(page, result.getBody());
        assertEquals(2, result.getBody().getContent().size());
        assertEquals(0, result.getBody().getNumber());
        verify(sessionService).getOpenSessionsForStudent(pageable);
    }

    @Test
    void getOpenSessionsForStudent_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<AttendanceSessionResponseDto> emptyPage =
                new PageImpl<>(List.of(), pageable, 0);

        when(sessionService.getOpenSessionsForStudent(pageable))
                .thenReturn(emptyPage);

        ResponseEntity<Page<AttendanceSessionResponseDto>> result =
                controller.getOpenSessionsForStudent(pageable);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
        verify(sessionService).getOpenSessionsForStudent(pageable);
    }

    @Test
    void getOpenSessionForTeacher_shouldReturnSession() {
        AttendanceSessionResponseDto response =
                new AttendanceSessionResponseDto();

        when(sessionService.getOpenSessionForTeacher())
                .thenReturn(response);

        ResponseEntity<AttendanceSessionResponseDto> result =
                controller.getOpenSessionForTeacher();

        assertEquals(200, result.getStatusCode().value());
        assertSame(response, result.getBody());
        verify(sessionService).getOpenSessionForTeacher();
    }

    @Test
    void countSessionsByClassId_shouldReturnCount() {
        Long classId = 10L;
        long expectedCount = 8L;

        when(sessionService.countSessionsByClassId(classId))
                .thenReturn(expectedCount);

        ResponseEntity<Long> result =
                controller.countSessionsByClassId(classId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(expectedCount, result.getBody());
        verify(sessionService).countSessionsByClassId(classId);
    }
}
