//package com.example.be.web.integration;
//
//import com.example.be.web.doman.dto.response.facedata.FaceResponse;
//import com.example.be.web.doman.entity.AttendanceSession;
//import com.example.be.web.doman.entity.Location;
//import com.example.be.web.doman.entity.User;
//import com.example.be.web.repository.AttendanceRecordRepository;
//import com.example.be.web.repository.AttendanceSessionRepository;
//import com.example.be.web.repository.LocationRepository;
//import com.example.be.web.repository.UserRepository;
//import com.example.be.web.security.UserPrincipal;
//import com.example.be.web.service.impl.FaceService;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.context.bean.override.mockito.MockitoBeans;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.RequestPostProcessor;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request
//        .SecurityMockMvcRequestPostProcessors.user;
//import static org.springframework.test.web.servlet.request
//        .MockMvcRequestBuilders.multipart;
//import static org.springframework.test.web.servlet.result
//        .MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//@ActiveProfiles("test")
//public class AttendanceCheckInIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private FaceService faceService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private AttendanceRecordRepository recordRepository;
//
//    @Autowired
//    private AttendanceSessionRepository sessionRepository;
//
//    @Autowired
//    private LocationRepository locationRepository;
//
//    private Long savedUserId;
//    private Long savedSessionId;
//
//    private MockMultipartFile validFaceImage() {
//        return new MockMultipartFile(
//                "faceImage",
//                "face.jpg",
//                MediaType.IMAGE_JPEG_VALUE,
//                new byte[]{1, 2, 3}
//        );
//    }
//
//    private RequestPostProcessor authenticateAs(Long userId) {
//        UserPrincipal principal = new UserPrincipal(
//                userId,
//                "Student One",
//                "student1",
//                "password",
//                List.of(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//
//        return user(principal);
//    }
//
//    @BeforeEach
//    void setUp() {
//        recordRepository.deleteAll();
//
//        User user = new User();
//        user.setUsername("student1");
//        user.setPassword("password");
//        user.setEmail("student1@example.com");
//        user.setLastLogin(LocalDateTime.now());
//        User savedUser = userRepository.save(user);
//        savedUserId = savedUser.getId();
//
//        // 2. Tạo và lưu Location (Khớp với tọa độ GPS test hoặc địa điểm mặc định)
//        Location location = new Location();
//        location.setLocationCode("Main Campus");
//        location.setLatitude(21.0285);
//        location.setLongitude(105.8542);
//        location.setRadiusMeters(100.0); // Bán kính cho phép check-in
//        Location savedLocation = locationRepository.save(location);
//
//        AttendanceSession session = new AttendanceSession();
//        session.setTitle("Test Session");
//        session.setStartTime(LocalDateTime.now().minusHours(1));
//        session.setEndTime(LocalDateTime.now().plusHours(1));
//        session.setCreateAt(LocalDateTime.now());
//        session.setUpdateAt(LocalDateTime.now());
//        session.setLocation(savedLocation);
//        AttendanceSession savedSession = sessionRepository.save(session);
//        savedSessionId = savedSession.getSessionId();
//
//    }
//
//    @Test
//    void checkIn_shouldReturn200WhenSuccessful() throws Exception {
//        double gpsLat = 21.0285;
//        double gpsLng = 105.8542;
//
//        FaceResponse faceResponse = FaceResponse.builder()
//                .name("student1")
//                .confidence(0.9)
//                .message("Face recognized")
//                .build();
//
//        when(faceService.recognize(any(MultipartFile.class), anyLong())).thenReturn(faceResponse);
//
//        mockMvc.perform(multipart("/api/attendance/checkin")
//                        .file(validFaceImage())
//                        .param("sessionId", savedSessionId.toString())
//                        .param("gpsLat", String.valueOf(gpsLat))
//                        .param("gpsLng", String.valueOf(gpsLng))
//                        .with(authenticateAs(savedUserId)))
//                .andExpect(status().isOk());
//
//        verify(faceService, times(1)).recognize(any(MultipartFile.class), anyLong());
//    }
//
//    @Test
//    void checkIn_shouldReturn400WhenAlreadyCheckedIn() throws Exception {
//        double gpsLat = 21.0285;
//        double gpsLng = 105.8542;
//
//        FaceResponse faceResponse = FaceResponse.builder()
//                .name("student1")
//                .confidence(0.9)
//                .message("Face recognized")
//                .build();
//
//        when(faceService.recognize(any(MultipartFile.class), anyLong())).thenReturn(faceResponse);
//
//        // Lần 1: Check-in lần đầu -> Thành công (200 OK)
//        mockMvc.perform(multipart("/api/attendance/checkin")
//                        .file(validFaceImage())
//                        .param("sessionId", savedSessionId.toString())
//                        .param("gpsLat", String.valueOf(gpsLat))
//                        .param("gpsLng", String.valueOf(gpsLng))
//                        .with(authenticateAs(savedUserId)))
//                .andExpect(status().isOk());
//
//        assertTrue(recordRepository.findByAttendanceSession_SessionIdAndUser_Id(savedSessionId, savedUserId).isPresent());
//
//        // Lần 2: Check-in lại với cùng User và Session -> Phải trả về lỗi 400
//        mockMvc.perform(multipart("/api/attendance/checkin")
//                        .file(validFaceImage())
//                        .param("sessionId", savedSessionId.toString())
//                        .param("gpsLat", String.valueOf(gpsLat))
//                        .param("gpsLng", String.valueOf(gpsLng))
//                        .with(authenticateAs(savedUserId)))
//                .andExpect(status().isBadRequest());
//
//        verify(faceService, times(1)).recognize(any(MultipartFile.class), eq(savedUserId));
//        // Không có bản ghi trùng thứ hai
//        assertEquals(1, recordRepository
//                .findAll().stream()
//                .filter(record ->
//                        record.getUser().getId().equals(savedUserId)
//                                && record.getAttendanceSession().getSessionId()
//                                .equals(savedSessionId))
//                .count());
//    }
//
//    @Test
//    void checkIn_shouldReturn401WhenUnauthenticated() throws Exception {
//        mockMvc.perform(multipart("/api/attendance/checkin")
//                        .file(validFaceImage())
//                        .param("sessionId", savedSessionId.toString())
//                        .param("gpsLat", "21.0285")
//                        .param("gpsLng", "105.8542"))
//                .andExpect(status().isUnauthorized());
//
//        // Request chưa xác thực thì không được gọi AI
//        verifyNoInteractions(faceService);
//    }
//
////    @Test
////    void checkIn_shouldReturn400WhenRecordExists() throws Exception {
////        double gpsLat = 21.0285;
////        double gpsLng = 105.8542;
////
////
////        // Tạo một bản ghi điểm danh trước để kiểm tra trường hợp đã tồn tại
////        AttendanceSession session = sessionRepository.findById(savedSessionId).orElseThrow();
////        User user = userRepository.findById(savedUserId).orElseThrow();
////
////        when(recordRepository.findByAttendanceSession_SessionIdAndUser_Id(savedSessionId, savedUserId))
////                .thenReturn(java.util.Optional.of(mock(com.example.be.web.doman.entity.AttendanceRecord.class)));
////
////        mockMvc.perform(multipart("/api/attendance/checkin")
////                        .file(validFaceImage())
////                        .param("sessionId", savedSessionId.toString())
////                        .param("gpsLat", String.valueOf(gpsLat))
////                        .param("gpsLng", String.valueOf(gpsLng))
////                        .with(authenticateAs(savedUserId)))
////                .andExpect(status().isBadRequest());
////
////        verify(faceService, never()).recognize(any(MultipartFile.class), anyLong());
////    }
//}
