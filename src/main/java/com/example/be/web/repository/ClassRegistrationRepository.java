package com.example.be.web.repository;

import com.example.be.web.doman.dto.response.classRoom.ClassRegistrationResponseDto;
import com.example.be.web.doman.entity.AttendanceSession;
import com.example.be.web.doman.entity.ClassRegistration;
import com.example.be.web.doman.entity.ClassRoom;
import com.example.be.web.doman.entity.User;
import com.example.be.web.doman.model.RegistrationStatus;
import com.example.be.web.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassRegistrationRepository extends JpaRepository<ClassRegistration, Long> {

    boolean existsByClassEntityAndStudent(ClassRoom classRoom, User student);
    Page<ClassRegistration> findByStudent(User student, Pageable pageable);
    Page<ClassRegistration> findByStudent_Id(Pageable pageable, Long userId);
    Page<ClassRegistration> findByClassEntity_ClassId(Pageable pageable, Long classId); //ủa nó có quy tắc gì vậy nhỉ
    Page<ClassRegistration> findByStudent_IdAndStatus(Pageable pageable, Long userId, RegistrationStatus status);
    Page<ClassRegistration> findByClassEntity_ClassIdAndStatus(Pageable pageable, Long classId, RegistrationStatus status);
//    Page<ClassRegistrationResponseDto> getRegistrationsByUser(Long userId, Pageable pageable, UserPrincipal principal);

    @Query("SELECT a FROM ClassRegistration r " +
            "JOIN r.classEntity c " +
            "JOIN c.attendanceSessions a " +
            "WHERE r.student.id = :userId " +
//            "AND a.status = AttendanceStatus.OPEN" +
            "AND r.status = RegistrationStatus.ACCEPTED "+
            "AND a.endTime > CURRENT_TIMESTAMP ")//cái này có ổn không nhỉ, có cần thêm điều kiện gì nữa không
    Page<AttendanceSession> findOpenSessionsByStudent(Long userId, Pageable pageable);
}
