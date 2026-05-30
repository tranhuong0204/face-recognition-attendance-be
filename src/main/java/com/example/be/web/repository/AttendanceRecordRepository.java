package com.example.be.web.repository;

import com.example.be.web.doman.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByAttendanceSession_SessionId(Long sessionId);

    List<AttendanceRecord> findByUser_Id(Long userId);

    @Query("""
            SELECT r
            FROM AttendanceRecord r
            WHERE r.attendanceSession.classRoom.classId = :classId
            """)
    List<AttendanceRecord> findByClassRoomId(@Param("classId") Long classId);
}