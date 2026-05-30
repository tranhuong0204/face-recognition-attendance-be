package com.example.be.web.service.impl;

import com.example.be.web.constant.ErrorMessage;
import com.example.be.web.doman.dto.request.classRoom.ClassRegistrationRequestDto;
import com.example.be.web.doman.dto.response.classRoom.ClassRegistrationResponseDto;
import com.example.be.web.doman.entity.ClassRegistration;
import com.example.be.web.doman.entity.ClassRoom;
import com.example.be.web.doman.entity.User;
import com.example.be.web.doman.mapper.ClassRegistrationMapper;
import com.example.be.web.doman.model.RegistrationStatus;
import com.example.be.web.exception.extended.ForbiddenException;
import com.example.be.web.exception.extended.InvalidException;
import com.example.be.web.exception.extended.NotFoundException;
import com.example.be.web.repository.ClassRegistrationRepository;
import com.example.be.web.repository.ClassRepository;
import com.example.be.web.repository.UserRepository;
import com.example.be.web.security.UserPrincipal;
import com.example.be.web.service.ClassRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassRegistrationServiceImpl implements ClassRegistrationService {

    private final ClassRegistrationRepository classRegistrationRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final ClassRegistrationMapper mapper;

    @Override
    public ClassRegistrationResponseDto registerStudentToClass(ClassRegistrationRequestDto requestDto) {
        ClassRoom classRoom = classRepository.findById(requestDto.getClassId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.ClassRoom.CLASS_NOT_FOUND,
                        new String[]{requestDto.getClassId().toString()}
                ));

        User student = userRepository.findById(requestDto.getStudentId())
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.User.USER_NOT_FOUND_ID,
                        new String[]{requestDto.getStudentId().toString()}
                ));

        if (classRegistrationRepository.existsByClassEntityAndStudent(classRoom, student)) {
            throw new InvalidException(ErrorMessage.ClassRegistration.REGISTERED);
        }

        ClassRegistration registration = ClassRegistration.builder()
                .classEntity(classRoom)
                .student(student)
                .status(RegistrationStatus.OPENING)
                .pending(true)
                .registeredAt(LocalDateTime.now())
                .build();

        ClassRegistration saved = classRegistrationRepository.save(registration);
        return mapper.toResponseDto(saved);
    }

    @Override
    public ClassRegistrationResponseDto updateRegistrationStatus(Long registrationId, RegistrationStatus status) {
        ClassRegistration registration = classRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.ClassRegistration.REGISTRATION_NOT_FOUND,
                        new String[]{registrationId.toString()}
                ));

        registration.setStatus(status);
        registration.setPending(false);

        ClassRegistration updated = classRegistrationRepository.save(registration);
        return mapper.toResponseDto(updated);
    }


    @Override
    public void deleteRegistration(Long registrationId) {
        ClassRegistration registration = classRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.ClassRegistration.REGISTRATION_NOT_FOUND,
                        new String[]{registrationId.toString()}
                ));
        classRegistrationRepository.delete(registration);
    }

    @Override
    public Page<ClassRegistrationResponseDto> getByStudentId(Pageable pageable, Long studentId) {
        Page<ClassRegistrationResponseDto> registrations = classRegistrationRepository.findByStudent_Id(pageable, studentId)
                .map(mapper::toResponseDto);
        return registrations;
    }

    @Override
    public Page<ClassRegistrationResponseDto> getByClassId(Pageable pageable, Long classId) {
        Page<ClassRegistrationResponseDto> registrations = classRegistrationRepository.findByClassEntity_ClassId(pageable, classId)
                .map(mapper::toResponseDto);
        return registrations;
    }



    @Override
    public Page<ClassRegistrationResponseDto> getByStudentIdAndStatus(Pageable pageable, Long studentId, RegistrationStatus status) {
        Page<ClassRegistrationResponseDto> registrations = classRegistrationRepository.findByStudent_IdAndStatus(pageable, studentId, status)
                .map(mapper::toResponseDto);
        return registrations;
    }

    @Override
    public Page<ClassRegistrationResponseDto> getByClassIdAndStatus(Pageable pageable, Long classId, RegistrationStatus status) {
        Page<ClassRegistrationResponseDto> registrations = classRegistrationRepository.findByClassEntity_ClassIdAndStatus(pageable, classId, status)
                .map(mapper::toResponseDto);
        return registrations;
    }

    @Override
    public Page<ClassRegistrationResponseDto> getRegistrationsByUser(Long userId, Pageable pageable, UserPrincipal principal) {
        if (!principal.getId().equals(userId) && !principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN_UPDATE_DELETE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND));


        Page<ClassRegistration> registrations = classRegistrationRepository.findByStudent(user, pageable);

        return registrations.map(this::toReponse);
    }

    public ClassRegistrationResponseDto toReponse(ClassRegistration reg) {
        return ClassRegistrationResponseDto.builder()
                .registrationId(reg.getRegistrationId())
                .classTitle(reg.getClassEntity().getTitle())
                .registeredAt(reg.getRegisteredAt())
                .pending(reg.isPending())

//                .RegistrationStatus(reg.getStatus().name())
//                .classRoom(classMapper.toDTO(reg.getClassEntity()))
                .build();
    }



}
