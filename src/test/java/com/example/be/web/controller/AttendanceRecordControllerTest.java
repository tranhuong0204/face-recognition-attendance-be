package com.example.be.web.controller;

import com.example.be.web.doman.dto.response.attendance.AttendanceRecordResponseDto;
import com.example.be.web.doman.dto.response.attendance.StudentAttendanceStatsResponseDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;
import com.example.be.web.service.AttendanceRecordService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceRecordControllerTest {

    @Mock
    private AttendanceRecordService attendanceRecordService;

    @InjectMocks
    private AttendanceRecordController controller;

    // ==================== CHECK IN ====================

    @Test
    void checkIn_shouldReturnFaceResponse() throws IOException {
        Long sessionId = 1L;
        double gpsLat = 21.0285;
        double gpsLng = 105.8542;

        MultipartFile faceImage = new MockMultipartFile(
                "faceImage",
                "face.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3}
        );

        FaceResponse expected = mock(FaceResponse.class);

        when(attendanceRecordService.checkIn(
                sessionId, faceImage, gpsLat, gpsLng
        )).thenReturn(expected);

        ResponseEntity<FaceResponse> response = controller.checkIn(
                sessionId, faceImage, gpsLat, gpsLng
        );

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());

        verify(attendanceRecordService).checkIn(
                sessionId, faceImage, gpsLat, gpsLng
        );
    }

    @Test
    void checkIn_shouldPropagateIOException() throws IOException {
        Long sessionId = 1L;

        MultipartFile faceImage = new MockMultipartFile(
                "faceImage",
                "face.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[]{1, 2, 3}
        );

        when(attendanceRecordService.checkIn(
                eq(sessionId), eq(faceImage), anyDouble(), anyDouble()
        )).thenThrow(new IOException("Image processing failed"));

        assertThrows(
                IOException.class,
                () -> controller.checkIn(
                        sessionId, faceImage, 21.0, 105.0
                )
        );
    }

    // ==================== GET RECORD BY ID ====================

    @Test
    void getRecordById_shouldReturnRecord() {
        Long recordId = 1L;

        AttendanceRecordResponseDto expected =
                mock(AttendanceRecordResponseDto.class);

        when(attendanceRecordService.getRecordById(recordId))
                .thenReturn(expected);

        ResponseEntity<AttendanceRecordResponseDto> response =
                controller.getRecordById(recordId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());

        verify(attendanceRecordService).getRecordById(recordId);
    }

    // ==================== GET RECORDS BY SESSION ====================

    @Test
    void getRecordsBySession_shouldReturnRecords() {
        Long sessionId = 1L;

        List<AttendanceRecordResponseDto> expected =
                List.of(
                        mock(AttendanceRecordResponseDto.class),
                        mock(AttendanceRecordResponseDto.class)
                );

        when(attendanceRecordService.getRecordsBySession(sessionId))
                .thenReturn(expected);

        ResponseEntity<List<AttendanceRecordResponseDto>> response =
                controller.getRecordsBySession(sessionId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(2, response.getBody().size());

        verify(attendanceRecordService).getRecordsBySession(sessionId);
    }

    @Test
    void getRecordsBySession_shouldReturnEmptyListWhenNoRecords() {
        Long sessionId = 1L;

        when(attendanceRecordService.getRecordsBySession(sessionId))
                .thenReturn(List.of());

        ResponseEntity<List<AttendanceRecordResponseDto>> response =
                controller.getRecordsBySession(sessionId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== GET RECORDS BY USER ====================

    @Test
    void getRecordsByUser_shouldReturnRecords() {
        Long userId = 10L;

        List<AttendanceRecordResponseDto> expected =
                List.of(mock(AttendanceRecordResponseDto.class));

        when(attendanceRecordService.getRecordsByUser(userId))
                .thenReturn(expected);

        ResponseEntity<List<AttendanceRecordResponseDto>> response =
                controller.getRecordsByUser(userId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());

        verify(attendanceRecordService).getRecordsByUser(userId);
    }

    // ==================== GET MY RECORDS ====================

    @Test
    void getMyRecords_shouldReturnCurrentUserRecords() {
        List<AttendanceRecordResponseDto> expected =
                List.of(mock(AttendanceRecordResponseDto.class));

        when(attendanceRecordService.getMyRecords())
                .thenReturn(expected);

        ResponseEntity<List<AttendanceRecordResponseDto>> response =
                controller.getMyRecords();

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());

        verify(attendanceRecordService).getMyRecords();
    }

    // ==================== GET STUDENT STATS BY CLASS ====================

    @Test
    void getStudentStatsByClass_shouldReturnStatistics() {
        Long classId = 5L;

        List<StudentAttendanceStatsResponseDto> expected =
                List.of(
                        mock(StudentAttendanceStatsResponseDto.class),
                        mock(StudentAttendanceStatsResponseDto.class)
                );

        when(attendanceRecordService.getStudentStatsByClass(classId))
                .thenReturn(expected);

        ResponseEntity<List<StudentAttendanceStatsResponseDto>> response =
                controller.getStudentStatsByClass(classId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        assertEquals(2, response.getBody().size());

        verify(attendanceRecordService).getStudentStatsByClass(classId);
    }
}
