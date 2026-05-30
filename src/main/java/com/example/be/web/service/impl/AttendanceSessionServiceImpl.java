package com.example.be.web.service.impl;

import com.example.be.web.constant.ErrorMessage;
import com.example.be.web.doman.dto.request.attendance.AttendanceSessionRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.doman.entity.AttendanceSession;
import com.example.be.web.doman.entity.ClassRoom;
import com.example.be.web.doman.entity.Location;
import com.example.be.web.doman.entity.User;
import com.example.be.web.doman.mapper.AttendanceSessionMapper;
import com.example.be.web.doman.model.AttendanceStatus;
import com.example.be.web.doman.model.RecordStatus;
import com.example.be.web.exception.extended.InternalServerException;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.*;
import com.example.be.web.security.UserPrincipal;
import com.example.be.web.service.AttendanceSessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceSessionServiceImpl implements AttendanceSessionService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceSessionMapper mapper;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ClassRepository classRepository;
    private final ClassRegistrationRepository classRegistrationRepository;


    @Override
    public AttendanceSessionResponseDto createSession(AttendanceSessionRequestDto requestDto) {

        if (requestDto.getLocationId() == null) {
            throw new NotFoundException("LOCATION_ID_NULL");
        }

        AttendanceSession session = mapper.toEntity(requestDto);

        ClassRoom classRoom = classRepository.findById(requestDto.getClassId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.ClassRoom.CLASS_NOT_FOUND,
                        new String[]{requestDto.getClassId().toString()}
                ));
        session.setClassRoom(classRoom);

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User creator = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.User.USER_NOT_FOUND_ID,
                        new String[]{principal.getId().toString()}
                ));

        session.setCreatedByUser(creator);

        Location location = locationRepository.findById(requestDto.getLocationId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.Location.LOCATION_NOT_FOUND,
                        new String[]{requestDto.getLocationId().toString()}
                ));

        session.setLocation(location);

        session.setCreateAt(LocalDateTime.now());
        session.setUpdateAt(LocalDateTime.now());
        session.setStatus(
                requestDto.getStatus() != null
                        ? requestDto.getStatus()
                        : AttendanceStatus.CLOSED
        );

//        session.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : AttendanceStatus.CLOSED);
        session.setStatus(AttendanceStatus.OPEN); // mặc định khi tạo buổi điểm danh sẽ có trạng thái là OPEN


        AttendanceSession saved = sessionRepository.save(session);

        return mapper.toResponse(saved);
    }

    @Override
    public AttendanceSessionResponseDto updateSession(Long id, AttendanceSessionRequestDto requestDto) {

        AttendanceSession existing = sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.AttendanceSession.SESSION_NOT_FOUND,
                        new String[]{id.toString()}
                ));

        mapper.updateEntityFromDto(requestDto, existing);
        existing.setUpdateAt(LocalDateTime.now());

        if (requestDto.getClassId() != null) {
            ClassRoom classRoom = classRepository.findById(requestDto.getClassId())
                    .orElseThrow(() -> new NotFoundException(
                            ErrorMessage.ClassRoom.CLASS_NOT_FOUND,
                            new String[]{requestDto.getClassId().toString()}
                    ));
            existing.setClassRoom(classRoom);
        }

        if (requestDto.getLocationId() != null) {
            Location location = locationRepository.findById(requestDto.getLocationId())
                    .orElseThrow(() -> new NotFoundException(
                            ErrorMessage.Location.LOCATION_NOT_FOUND,
                            new String[]{requestDto.getLocationId().toString()}
                    ));
            existing.setLocation(location);
        }

        AttendanceSession updated = sessionRepository.save(existing);
        return mapper.toResponse(updated);
    }

    @Override
    public void deleteSession(Long id) {
        AttendanceSession existing = sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.AttendanceSession.SESSION_NOT_FOUND,
                        new String[]{id.toString()}
                ));
        sessionRepository.delete(existing);
    }

    @Override
    public AttendanceSessionResponseDto getSessionById(Long id) {
        AttendanceSession entity = sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.AttendanceSession.SESSION_NOT_FOUND,
                        new String[]{id.toString()}
                ));
        return mapper.toResponse(entity);
    }

    @Override
    public List<AttendanceSessionResponseDto> getAllSessions() {
        try {
            List<AttendanceSession> entities = sessionRepository.findAll();
            return mapper.toResponses(entities);
        } catch (Exception e) {
            log.error("Error fetching AttendanceSessions: {}", e.getMessage(), e);
            throw new InternalServerException(ErrorMessage.AttendanceSession.ERR_GET_ALL_SESSION);
        }
    }

    @Override
    public long countPresentStudentsInSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.AttendanceSession.SESSION_NOT_FOUND,
                        new String[]{sessionId.toString()}
                ));
        return session.getAttendanceRecords().stream()
                .filter(record -> record.getRecordStatus() == RecordStatus.PRESENT)
                .count();
    }

    @Override
    public Page<AttendanceSessionResponseDto> getOpenSessionsForStudent(Pageable pageable) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.USER_NOT_FOUND_ID, new String[]{principal.getId().toString()}));

        Page<AttendanceSession> sessions = classRegistrationRepository.findOpenSessionsByStudent(user.getId(), pageable);

        return sessions.map(mapper::toResponse);
    }

    @Override
    public AttendanceSessionResponseDto getOpenSessionForTeacher() {
        // lấy user đang đăng nhập từ SecurityContext
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.USER_NOT_FOUND_ID, new String[]{principal.getId().toString()}));

        AttendanceSession session = sessionRepository.findOpenSessionForTeacher(user.getId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.AttendanceSession.OPEN_SESSION_NOT_FOUND_FOR_TEACHER,
                        new String[]{user.getId().toString()}
                ));
        return mapper.toResponse(session);
    }
}