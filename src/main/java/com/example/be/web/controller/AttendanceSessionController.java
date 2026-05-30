package com.example.be.web.controller;

import com.example.be.web.doman.dto.request.attendance.AttendanceSessionRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.service.AttendanceSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class AttendanceSessionController {

    private final AttendanceSessionService sessionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "API tạo buổi điểm danh", description = "Admin / Leader")
    @Tag(name = "admin_leader")
    public ResponseEntity<AttendanceSessionResponseDto> createSession(
            @RequestBody AttendanceSessionRequestDto requestDto) {
        AttendanceSessionResponseDto created = sessionService.createSession(requestDto);
        return ResponseEntity.ok(created);
    }

    // 2. Cập nhật buổi điểm danh
    @Tag(name = "attendanceSession")

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "API cập nhật buổi điểm danh", description = "Admin / Leader")
    public ResponseEntity<AttendanceSessionResponseDto> updateSession(
            @PathVariable Long id,
            @RequestBody AttendanceSessionRequestDto requestDto) {
        AttendanceSessionResponseDto updated = sessionService.updateSession(id, requestDto);
        return ResponseEntity.ok(updated);
    }


    // 3. Xóa buổi điểm danh
    @Tag(name = "attendanceSession")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "API xóa buổi điểm danh theo id", description = "Admin / Leader")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','USER')")
    @Operation(summary = "API lấy buổi điểm danh theo id", description = "Admin / Leader / User")

    public ResponseEntity<AttendanceSessionResponseDto> getSessionById(@PathVariable Long id) {
        AttendanceSessionResponseDto session = sessionService.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','USER')")
    @Operation(summary = "API lấy tất cả buổi điểm danh", description = "Admin / Leader / User")
    public ResponseEntity<List<AttendanceSessionResponseDto>> getAllSessions() {
        List<AttendanceSessionResponseDto> sessions = sessionService.getAllSessions();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/{sessionId}/present-count")
    @Tag(name = "admin_leader")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "API đếm số lượng sinh viên đã điểm danh có mặt trong buổi điểm danh", description = "Admin / Leader")
    public ResponseEntity<Long> countPresentStudentsInSession(@PathVariable Long sessionId) {
        long count = sessionService.countPresentStudentsInSession(sessionId);
        return ResponseEntity.ok(count);
    }

    @Tag(name = "attendanceSession")
    @PreAuthorize("hasAnyRole('USER')")
    @GetMapping("/open")
    @Operation(summary = "API lấy danh sách buổi điểm danh đang mở cho sinh viên", description = "User")
    public ResponseEntity<Page<AttendanceSessionResponseDto>> getOpenSessionsForStudent(@ParameterObject Pageable pageable) {
        Page<AttendanceSessionResponseDto> sessions = sessionService.getOpenSessionsForStudent(pageable);
        return ResponseEntity.ok(sessions);
    }

    @Tag(name = "attendanceSession")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @GetMapping("/open/teacher")
    @Operation(summary = "API lấy buổi điểm danh đang mở cho giáo viên", description = "Leader")
    public ResponseEntity<AttendanceSessionResponseDto> getOpenSessionForTeacher() {
        AttendanceSessionResponseDto session = sessionService.getOpenSessionForTeacher();
        return ResponseEntity.ok(session);
    }

}
