package com.example.be.web.controller;

import com.example.be.web.doman.dto.response.attendance.AttendanceRecordResponseDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;
import com.example.be.web.doman.dto.response.attendance.StudentAttendanceStatsResponseDto;
import com.example.be.web.service.AttendanceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    // 1. User điểm danh / check-in
    @PostMapping(
            value = "/checkin",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(summary = "API record by user", description = "User check-in attendance")
    public ResponseEntity<FaceResponse> checkIn(
            @RequestParam("sessionId") Long sessionId,
            @RequestParam("faceImage") MultipartFile faceImage,
            @RequestParam("gpsLat") double gpsLat,
            @RequestParam("gpsLng") double gpsLng
    ) throws IOException {
        FaceResponse record =
                attendanceRecordService.checkIn(sessionId, faceImage, gpsLat, gpsLng);

        return ResponseEntity.ok(record);
    }

    // 2. Admin / Leader lấy record theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    @Operation(summary = "API get record by id", description = "Admin / Leader")
    public ResponseEntity<AttendanceRecordResponseDto> getRecordById(
            @PathVariable Long id
    ) {
        AttendanceRecordResponseDto record = attendanceRecordService.getRecordById(id);
        return ResponseEntity.ok(record);
    }

    // 3. Admin / Leader lấy danh sách record theo session
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    @Operation(summary = "API get record by session", description = "Admin / Leader")
    public ResponseEntity<List<AttendanceRecordResponseDto>> getRecordsBySession(
            @PathVariable Long sessionId
    ) {
        List<AttendanceRecordResponseDto> records =
                attendanceRecordService.getRecordsBySession(sessionId);

        return ResponseEntity.ok(records);
    }

    // 4. Admin / Leader xem lịch sử của một user bất kỳ theo userId
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    @Operation(summary = "API get records by userId", description = "Admin / Leader xem danh sách record của một user cụ thể")
    public ResponseEntity<List<AttendanceRecordResponseDto>> getRecordsByUser(
            @PathVariable Long userId
    ) {
        List<AttendanceRecordResponseDto> records =
                attendanceRecordService.getRecordsByUser(userId);

        return ResponseEntity.ok(records);
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(summary = "API get records of current user", description = "User xem danh sách record của chính mình")
    public ResponseEntity<List<AttendanceRecordResponseDto>> getMyRecords() {
        List<AttendanceRecordResponseDto> records = attendanceRecordService.getMyRecords();
        return ResponseEntity.ok(records);
    }

    //thống kê theo từng lớp
    @GetMapping("/class/{classId}/student-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    @Operation(summary = "API thống kê điểm danh từng sinh viên theo lớp", description = "Admin / Leader")
    public ResponseEntity<List<StudentAttendanceStatsResponseDto>> getStudentStatsByClass(
            @PathVariable Long classId
    ) {
        return ResponseEntity.ok(attendanceRecordService.getStudentStatsByClass(classId));
    }
}
