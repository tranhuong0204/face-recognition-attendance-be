package com.example.be.web.repository;

import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.doman.entity.AttendanceSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Page<AttendanceSessionResponseDto> findAllByClassRoom_ClassId(Long classId, Pageable pageable);
    @Query("SELECT a FROM AttendanceSession a WHERE a.endTime > CURRENT_TIMESTAMP AND a.status = AttendanceStatus.OPEN AND a.createdByUser.id = :teacherId")
    Optional<AttendanceSession> findOpenSessionForTeacher(@Param("teacherId") Long teacherId);
//    Page<AttendanceSession> findOpenSessionsForTeacher(@Param("teacherId") Long teacherId , Pageable pageable);


}

