package com.example.be.web.service;

import com.example.be.web.doman.dto.response.attendance.AttendanceRecordResponseDto;
import com.example.be.web.doman.dto.response.facedata.FaceResponse;

import com.example.be.web.doman.dto.response.attendance.StudentAttendanceStatsResponseDto;

import com.example.be.web.doman.entity.AttendanceRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttendanceRecordService {
    FaceResponse checkIn(Long sessionId, MultipartFile faceImage, double gpsLat, double gpsLng) throws IOException;
//    AttendanceRecord checkIn(Long sessionId, Long userId, AttendanceRecord record);
    AttendanceRecordResponseDto  getRecordById(Long id);
    List<AttendanceRecordResponseDto > getRecordsBySession(Long sessionId);
    List<AttendanceRecordResponseDto > getRecordsByUser(Long userId);
    // Cho user tự xem mình
    List<AttendanceRecordResponseDto> getMyRecords();
    List<StudentAttendanceStatsResponseDto> getStudentStatsByClass(Long classId);
}

