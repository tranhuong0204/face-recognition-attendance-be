package com.example.be.web.service.impl;

import com.example.be.web.doman.dto.request.attendance.AttendanceRecordRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceRecordResponseDto;
import com.example.be.web.doman.dto.response.attendance.StudentAttendanceStatsResponseDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;
import com.example.be.web.doman.entity.AttendanceRecord;
import com.example.be.web.doman.entity.AttendanceSession;
import com.example.be.web.doman.entity.Location;
import com.example.be.web.doman.entity.User;
import com.example.be.web.doman.mapper.AttendanceRecordMapper;
import com.example.be.web.exception.extended.BadRequestException;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.AttendanceRecordRepository;
import com.example.be.web.repository.AttendanceSessionRepository;
import com.example.be.web.repository.LocationRepository;
import com.example.be.web.repository.UserRepository;
import com.example.be.web.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.be.web.doman.model.RecordStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AttendanceRecordServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private AttendanceSessionRepository sessionRepository;

    @Mock
    private AttendanceRecordResponseDto attendanceRecordResponseDto;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private AttendanceRecordMapper mapper;

    @Mock
    private FaceService faceService;

    @InjectMocks
    private AttendanceRecordServiceImpl attendanceRecordService;

    private void mockCurrentUser(Long userId) {
        UserPrincipal principal = new UserPrincipal(userId,"fullname", "student1", "password", Collections.emptyList());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.emptyList()
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void checkIn_shouldSaveRecordWhenFaceMatches() throws Exception {
        Long sessionId = 1L;
        Long userId = 10L;
        double gpsLat = 21.0285;
        double gpsLng = 105.8542;

        Location location = Location.builder().build();
        AttendanceSession session = AttendanceSession.builder().location(location).build();
        User user = User.builder().id(userId).username("student1").build();
        MockMultipartFile faceImage = new MockMultipartFile("faceImage", "face.jpg", "image/jpeg", new byte[]{1, 2, 3});
        FaceResponse recognize = mock(FaceResponse.class);
        when(recognize.getConfidence()).thenReturn(0.85);

        AttendanceRecord record = new AttendanceRecord();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(faceService.recognize(faceImage, userId))
                .thenReturn(recognize);
        when(mapper.toEntity(any(AttendanceRecordRequestDto.class)))
                .thenReturn(record);
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenReturn(record);

        mockCurrentUser(userId);

        FaceResponse result = attendanceRecordService.checkIn(sessionId, faceImage, gpsLat, gpsLng);
        assertEquals(recognize, result);

        verify(attendanceRecordRepository).save(record);
        verify(mapper).toEntity(any(AttendanceRecordRequestDto.class));
        verify(recognize).setMessage("Check-in success");

        assertSame(session, record.getAttendanceSession());
        assertSame(user, record.getUser());
    }

    @Test
    void checkIn_shouldNotSaveRecordWhenConfidenceIsTooLow() throws Exception {
        Long sessionId = 1L;
        Long userId = 10L;

        AttendanceSession session = AttendanceSession.builder()
                .location(Location.builder().build())
                .build();
        User user = User.builder()
                .id(userId)
                .username("student1")
                .build();

        MockMultipartFile faceImage = new MockMultipartFile(
                "faceImage", "face.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        FaceResponse recognize = mock(FaceResponse.class);
        when(recognize.getConfidence()).thenReturn(0.5);

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(faceService.recognize(faceImage, userId))
                .thenReturn(recognize);

        mockCurrentUser(userId);

        FaceResponse result = attendanceRecordService.checkIn(
                sessionId, faceImage, 21.0285, 105.8542
        );

        assertSame(recognize, result);
        verify(recognize).setMessage("Face not match");
        verify(attendanceRecordRepository, never()).save(any());
        verifyNoInteractions(mapper);
    }

    @Test
    void checkIn_shouldThrowExceptionWhenSessionNotFound() {
        Long sessionId = 999L;

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> attendanceRecordService.checkIn(
                        sessionId, null, 21.0285, 105.8542
                )
        );

        verifyNoInteractions(faceService);
        verify(attendanceRecordRepository, never())
                .save(any(AttendanceRecord.class));
    }

    @Test
    void checkIn_shouldThrowExceptionWhenUserNotFound() {
        Long sessionId = 1L;
        Long userId = 10L;

        mockCurrentUser(userId);

        AttendanceSession session = AttendanceSession.builder()
                .location(Location.builder().build())
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> attendanceRecordService.checkIn(
                        sessionId, null, 21.0285, 105.8542
                )
        );

        verifyNoInteractions(faceService);
        verify(attendanceRecordRepository, never())
                .save(any(AttendanceRecord.class));
    }

    @Test
    void checkIn_shouldThrowExceptionWhenLocationIsNull() {
        Long sessionId = 1L;
        Long userId = 10L;

        mockCurrentUser(userId);

        AttendanceSession session = AttendanceSession.builder()
                .location(null)
                .build();

        User user = User.builder()
                .id(userId)
                .username("student1")
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(
                NotFoundException.class,
                () -> attendanceRecordService.checkIn(
                        sessionId, null, 21.0285, 105.8542
                )
        );

        verifyNoInteractions(faceService);
        verify(attendanceRecordRepository, never())
                .save(any(AttendanceRecord.class));
    }

    @Test
    void checkIn_shouldThrowExceptionWhenRecordAlreadyExists() throws Exception {
        Long sessionId = 1L;
        Long userId = 10L;

        mockCurrentUser(userId);

        AttendanceSession session = AttendanceSession.builder()
                .location(Location.builder().build())
                .build();

        User user = User.builder()
                .id(userId)
                .username("student1")
                .build();

        MockMultipartFile faceImage = new MockMultipartFile(
                "faceImage",
                "face.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        AttendanceRecord existingRecord = AttendanceRecord.builder()
                .user(user)
                .attendanceSession(session)
                .recordStatus(RecordStatus.PRESENT)
                .build();

        when(sessionRepository.findById(sessionId))
                .thenReturn(Optional.of(session));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(attendanceRecordRepository
                .findByAttendanceSession_SessionIdAndUser_Id(sessionId, userId))
                .thenReturn(Optional.of(existingRecord));

        assertThrows(
                BadRequestException.class,
                () -> attendanceRecordService.checkIn(
                        sessionId, faceImage, 21.0285, 105.8542
                )
        );

        verify(attendanceRecordRepository)
                .findByAttendanceSession_SessionIdAndUser_Id(sessionId, userId);

        verifyNoInteractions(faceService);

        verify(attendanceRecordRepository, never())
                .save(any(AttendanceRecord.class));

        verifyNoInteractions(mapper);
    }

    @Test
    void getRecordById_shouldReturnRecordWhenFound() {
        Long recordId = 1L;
        AttendanceRecord record = AttendanceRecord.builder().build();
        //laays kieeur DTO theo kieu tra ve
        var expectedDto = mock(com.example.be.web.doman.dto.response.attendance.AttendanceRecordResponseDto.class);

        when(attendanceRecordRepository.findById(recordId)).thenReturn(java.util.Optional.of(record));
        when(mapper.toResponse(record)).thenReturn(expectedDto);

        var result = attendanceRecordService.getRecordById(recordId);

        assertEquals(expectedDto, result);
        verify(attendanceRecordRepository).findById(recordId);
        verify(mapper).toResponse(record);
    }

    @Test
    void  getRecordById_shouldThrowNotFoundExceptionWhenNotFound(){
        Long recordId = 999L;

        when(attendanceRecordRepository.findById(recordId)).thenReturn(java.util.Optional.empty());

        assertThrows(NotFoundException.class, () -> attendanceRecordService.getRecordById(recordId));
        verify(attendanceRecordRepository).findById(recordId);
        verifyNoInteractions(mapper);
    }

    @Test
    void getRecordsBySession_shouldReturnRecordsSuccessfully() {
        Long ssessionId = 1L;
        AttendanceRecord record1 = AttendanceRecord.builder().build();
        AttendanceRecord record2 = AttendanceRecord.builder().build();

        List<AttendanceRecord> records = List.of(record1, record2);

        AttendanceRecordResponseDto dto1 = mock(AttendanceRecordResponseDto.class);
        AttendanceRecordResponseDto dto2 = mock(AttendanceRecordResponseDto.class);

        List<AttendanceRecordResponseDto> expectedDtos = List.of(dto1, dto2);

        when(attendanceRecordRepository.findByAttendanceSession_SessionId(ssessionId)).thenReturn(records);
        when(mapper.toResponseList(records)).thenReturn(expectedDtos);

        List<AttendanceRecordResponseDto> result = attendanceRecordService.getRecordsBySession(ssessionId);

        assertEquals(expectedDtos, result);
        verify(attendanceRecordRepository).findByAttendanceSession_SessionId(ssessionId);
        verify(mapper).toResponseList(records);
    }

    @Test
    void getRecordsByUser_shouldReturnRecordsSuccessfully() {
        Long userId = 10L;

        AttendanceRecord record = AttendanceRecord.builder().build();
        List<AttendanceRecord> records = List.of(record);

        AttendanceRecordResponseDto dto =
                mock(AttendanceRecordResponseDto.class);
        List<AttendanceRecordResponseDto> expected = List.of(dto);

        when(attendanceRecordRepository.findByUser_Id(userId))
                .thenReturn(records);
        when(mapper.toResponseList(records)).thenReturn(expected);

        List<AttendanceRecordResponseDto> result =
                attendanceRecordService.getRecordsByUser(userId);

        assertEquals(expected, result);

        verify(attendanceRecordRepository).findByUser_Id(userId);
        verify(mapper).toResponseList(records);
    }

    @Test
    void getRecordsByUser() {
    }

    @Test
    void getMyRecords_shouldReturnCurrentUserRecords() {
        Long userId = 10L;
        mockCurrentUser(userId);

        AttendanceRecord record = AttendanceRecord.builder().build();
        List<AttendanceRecord> records = List.of(record);

        AttendanceRecordResponseDto dto =
                mock(AttendanceRecordResponseDto.class);
        List<AttendanceRecordResponseDto> expected = List.of(dto);

        when(attendanceRecordRepository.findByUser_Id(userId))
                .thenReturn(records);
        when(mapper.toResponseList(records)).thenReturn(expected);

        List<AttendanceRecordResponseDto> result =
                attendanceRecordService.getMyRecords();

        assertSame(expected, result);

        verify(attendanceRecordRepository).findByUser_Id(userId);
        verify(mapper).toResponseList(records);
    }

    @Test
    void getStudentStatsByClass_shouldCalculateCorrectPercentage() {
        Long classId = 1L;
        // Mock the repository to return a list of records for the class

        User user = User.builder().id(10L).username("student1").build();

        AttendanceRecord present1 = AttendanceRecord.builder().user(user).recordStatus(RecordStatus.PRESENT).build();
        AttendanceRecord present2 = AttendanceRecord.builder().user(user).recordStatus(RecordStatus.PRESENT).build();
        AttendanceRecord absent1 = AttendanceRecord.builder().user(user).recordStatus(RecordStatus.ABSENT).build();

        // Mock the repository to return these records
        when(attendanceRecordRepository.findByClassRoomId(classId)).thenReturn(List.of(present1, present2, absent1));

        List<StudentAttendanceStatsResponseDto> stats = attendanceRecordService.getStudentStatsByClass(classId);

        // Verify the results
        assertEquals(1, stats.size());
        StudentAttendanceStatsResponseDto stat = stats.get(0);
        assertEquals(user.getId(), stat.getStudentId());
        assertEquals(2, stat.getPresent());
        assertEquals(1, stat.getAbsent());
        assertEquals(66.67, stat.getPercent(), 0.01);

        verify(attendanceRecordRepository).findByClassRoomId(classId);
    }

    @Test
    void getStudentStatsByClass_shouldHandleNoRecords() {
        Long classId = 1L;
        when(attendanceRecordRepository.findByClassRoomId(classId)).thenReturn(List.of());

        List<StudentAttendanceStatsResponseDto> stats = attendanceRecordService.getStudentStatsByClass(classId);

        assertNotNull(stats);
        assertTrue(stats.isEmpty());
        verify(attendanceRecordRepository).findByClassRoomId(classId);
    }

    @Test
    void getStudentStatsByClass_shouldIgnoreRecordWithoutUser(){
        Long classId = 1L;

        AttendanceRecord record = AttendanceRecord. builder().user(null).recordStatus(RecordStatus.PRESENT).build();
        when(attendanceRecordRepository.findByClassRoomId(classId)).thenReturn(List.of(record));

        List<StudentAttendanceStatsResponseDto> records = attendanceRecordService.getStudentStatsByClass(classId);
        assertNotNull(records);
        assertTrue(records.isEmpty());

        verify(attendanceRecordRepository).findByClassRoomId(classId);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}