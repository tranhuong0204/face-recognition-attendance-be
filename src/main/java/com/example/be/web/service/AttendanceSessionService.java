package com.example.be.web.service;

import com.example.be.web.doman.dto.request.attendance.AttendanceSessionRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.doman.entity.AttendanceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AttendanceSessionService {
    AttendanceSessionResponseDto createSession(AttendanceSessionRequestDto requestDto);
    AttendanceSessionResponseDto updateSession(Long id, AttendanceSessionRequestDto requestDto);
    void deleteSession(Long id);
    AttendanceSessionResponseDto getSessionById(Long id);
    List<AttendanceSessionResponseDto> getAllSessions();
//    Page<AttendanceSessionResponseDto> getSessions(Pageable pageable);
    long countPresentStudentsInSession(Long sessionId);
    Page<AttendanceSessionResponseDto> getOpenSessionsForStudent(Pageable pageable);
    AttendanceSessionResponseDto getOpenSessionForTeacher();

}


