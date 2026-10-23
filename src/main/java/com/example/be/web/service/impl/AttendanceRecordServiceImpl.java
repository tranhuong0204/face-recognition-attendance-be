package com.example.be.web.service.impl;

import com.example.be.web.constant.ErrorMessage;
import com.example.be.web.doman.dto.request.attendance.AttendanceRecordRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceRecordResponseDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;
import com.example.be.web.doman.entity.*;
import com.example.be.web.doman.dto.response.attendance.StudentAttendanceStatsResponseDto;
import com.example.be.web.doman.entity.AttendanceRecord;
import com.example.be.web.doman.entity.AttendanceSession;
import com.example.be.web.doman.entity.Location;
import com.example.be.web.doman.entity.User;
import com.example.be.web.doman.mapper.AttendanceRecordMapper;
import com.example.be.web.doman.model.RecordStatus;
import com.example.be.web.exception.extended.BadRequestException;
import com.example.be.web.exception.extended.InternalServerException;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.AttendanceRecordRepository;
import com.example.be.web.repository.AttendanceSessionRepository;
import com.example.be.web.repository.LocationRepository;
import com.example.be.web.repository.UserRepository;
import com.example.be.web.security.UserPrincipal;
import com.example.be.web.service.AttendanceRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final AttendanceRecordMapper mapper;
    private final FaceService faceService;

    @Override
    public FaceResponse checkIn(Long sessionId, MultipartFile faceImage, double gpsLat, double gpsLng) throws IOException {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.AttendanceSession.SESSION_NOT_FOUND));

        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.USER_NOT_FOUND_ID));

        Location location = session.getLocation();
        if (location == null) {
            throw new NotFoundException(ErrorMessage.Location.LOCATION_NOT_FOUND);
        }

        // 1. Kiểm tra GPS - tạm ẩn
//    double distance = calculateDistance(location.getLatitude(), location.getLongitude(), gpsLat, gpsLng);
//    if (distance > location.getRadiusMeters()) {
//        throw new BadRequestException(ErrorMessage.AttendanceRecord.OUT_OF_RANGE);
//    }

        // 2. Kiểm tra thời gian điểm danh - tạm ẩn
//    LocalDateTime now = LocalDateTime.now();
//    if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
//        throw new BadRequestException(ErrorMessage.AttendanceRecord.OUT_OF_TIME);
//    }
        if (recordRepository.findByAttendanceSession_SessionIdAndUser_Id(sessionId, userId).isPresent()) {
            throw new BadRequestException(ErrorMessage.AttendanceRecord.RECORD_EXISTS);
        }

        // 3. Gọi AI nhận diện khuôn mặt
        FaceResponse recognize = faceService.recognize(faceImage, userId);

        log.info("FaceResponse: name={}, confidence={}, message={}",
                recognize.getName(), recognize.getConfidence(), recognize.getMessage());

        double confidence = recognize.getConfidence();

        // Nếu AI không nhận diện được hoặc độ tin cậy thấp thì KHÔNG lưu vào DB
        if (confidence <= 0 || confidence < 0.6) {
            recognize.setMessage("Face not match");
            return recognize;
        }

        // Chỉ đến đây mới là khuôn mặt đúng, lúc này mới lưu DB
        AttendanceRecordRequestDto dto = AttendanceRecordRequestDto.builder()
                .checkinTime(LocalDateTime.now())
                .gpsLatitude(gpsLat)
                .gpsLongitude(gpsLng)
                .resultFace(true)
                .recordStatus(RecordStatus.PRESENT)
                .build();

        AttendanceRecord record = mapper.toEntity(dto);
        record.setAttendanceSession(session);
        record.setUser(user);

        AttendanceRecord saved = recordRepository.save(record);

        recognize.setMessage("Check-in success");
        return recognize;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }


    @Override
    public AttendanceRecordResponseDto getRecordById(Long id) {
        AttendanceRecord record = recordRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorMessage.AttendanceRecord.RECORD_NOT_FOUND, new String[]{id.toString()}));
        return mapper.toResponse(record);
    }

    @Override
    public List<AttendanceRecordResponseDto> getRecordsBySession(Long sessionId) {
        try {
            List<AttendanceRecord> records = recordRepository.findByAttendanceSession_SessionId(sessionId);
            return mapper.toResponseList(records);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách record của session {}: {}", sessionId, e.getMessage());
            throw new InternalServerException(ErrorMessage.AttendanceRecord.ERR_GET_BY_SESSION);
        }
    }

    @Override
    public List<AttendanceRecordResponseDto> getRecordsByUser(Long userId) {
        try {
            List<AttendanceRecord> records = recordRepository.findByUser_Id(userId);
            return mapper.toResponseList(records);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách record của user {}: {}", userId, e.getMessage());
            throw new InternalServerException(ErrorMessage.AttendanceRecord.ERR_GET_BY_USER);
        }
    }

    @Override
    public List<AttendanceRecordResponseDto> getMyRecords() {
        Long userId = getCurrentUserId();
        List<AttendanceRecord> records = recordRepository.findByUser_Id(userId);
        return mapper.toResponseList(records);
    }

    // thống kê sinh viên theo lớp học
    @Override
    public List<StudentAttendanceStatsResponseDto> getStudentStatsByClass(Long classId) {
        List<AttendanceRecord> records =
                recordRepository.findByClassRoomId(classId);

        Map<Long, StudentAttendanceStatsResponseDto> result = new LinkedHashMap<>();

        for (AttendanceRecord record : records) {
            if (record.getUser() == null) continue;

            Long studentId = record.getUser().getId();
            String studentName = record.getUser().getUsername();

            StudentAttendanceStatsResponseDto dto = result.getOrDefault(
                    studentId,
                    StudentAttendanceStatsResponseDto.builder()
                            .studentId(studentId)
                            .studentName(studentName)
                            .present(0)
                            .absent(0)
                            .percent(0)
                            .build()
            );

            if (record.getRecordStatus() == RecordStatus.PRESENT) {
                dto.setPresent(dto.getPresent() + 1);
            } else {
                dto.setAbsent(dto.getAbsent() + 1);
            }

            result.put(studentId, dto);
        }

        for (StudentAttendanceStatsResponseDto dto : result.values()) {
            long total = dto.getPresent() + dto.getAbsent();
            double percent = total == 0 ? 0 : (dto.getPresent() * 100.0 / total);
            dto.setPercent(percent);
        }

        return new ArrayList<>(result.values());
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // bán kính Trái Đất (mét)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // khoảng cách tính bằng mét
    }

}
