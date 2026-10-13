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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassRegistrationServiceImplTest {

    @Mock
    private ClassRegistrationRepository classRegistrationRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassRegistrationMapper mapper;

    @InjectMocks
    private ClassRegistrationServiceImpl service;

    // 1. Đăng ký sinh viên thành công
    @Test
    void registerStudentToClass_shouldRegisterSuccessfully() {
        Long classId = 1L;
        Long studentId = 2L;

        ClassRegistrationRequestDto request =
                mock(ClassRegistrationRequestDto.class);
        when(request.getClassId()).thenReturn(classId);
        when(request.getStudentId()).thenReturn(studentId);

        ClassRoom classRoom = new ClassRoom();
        User student = new User();
        ClassRegistration saved = new ClassRegistration();
        ClassRegistrationResponseDto expected =
                new ClassRegistrationResponseDto();

        when(classRepository.findById(classId))
                .thenReturn(Optional.of(classRoom));
        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));
        when(classRegistrationRepository
                .existsByClassEntityAndStudent(classRoom, student))
                .thenReturn(false);
        when(classRegistrationRepository.save(any(ClassRegistration.class)))
                .thenReturn(saved);
        when(mapper.toResponseDto(saved)).thenReturn(expected);

        ClassRegistrationResponseDto result =
                service.registerStudentToClass(request);

        assertSame(expected, result);

        verify(classRegistrationRepository)
                .existsByClassEntityAndStudent(classRoom, student);
        verify(classRegistrationRepository).save(
                argThat(registration ->
                        registration.getClassEntity() == classRoom
                                && registration.getStudent() == student
                                && registration.getStatus()
                                == RegistrationStatus.PENDING
                                && registration.isPending()
                                && registration.getRegisteredAt() != null
                )
        );
        verify(mapper).toResponseDto(saved);
    }

    // 2. Đăng ký thất bại vì lớp không tồn tại
    @Test
    void registerStudentToClass_shouldThrowWhenClassNotFound() {
        Long classId = 1L;

        ClassRegistrationRequestDto request =
                mock(ClassRegistrationRequestDto.class);
        when(request.getClassId()).thenReturn(classId);

        when(classRepository.findById(classId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.registerStudentToClass(request)
        );

        verify(userRepository, never()).findById(any());
        verify(classRegistrationRepository, never())
                .save(any(ClassRegistration.class));
    }

    // 3. Đăng ký thất bại vì sinh viên không tồn tại
    @Test
    void registerStudentToClass_shouldThrowWhenStudentNotFound() {
        Long classId = 1L;
        Long studentId = 2L;

        ClassRegistrationRequestDto request =
                mock(ClassRegistrationRequestDto.class);
        when(request.getClassId()).thenReturn(classId);
        when(request.getStudentId()).thenReturn(studentId);

        when(classRepository.findById(classId))
                .thenReturn(Optional.of(new ClassRoom()));
        when(userRepository.findById(studentId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.registerStudentToClass(request)
        );

        verify(classRegistrationRepository, never())
                .save(any(ClassRegistration.class));
    }

    // 4. Không cho đăng ký trùng
    @Test
    void registerStudentToClass_shouldThrowWhenAlreadyRegistered() {
        Long classId = 1L;
        Long studentId = 2L;
        ClassRoom classRoom = new ClassRoom();
        User student = new User();

        ClassRegistrationRequestDto request =
                mock(ClassRegistrationRequestDto.class);
        when(request.getClassId()).thenReturn(classId);
        when(request.getStudentId()).thenReturn(studentId);

        when(classRepository.findById(classId))
                .thenReturn(Optional.of(classRoom));
        when(userRepository.findById(studentId))
                .thenReturn(Optional.of(student));
        when(classRegistrationRepository
                .existsByClassEntityAndStudent(classRoom, student))
                .thenReturn(true);

        assertThrows(
                InvalidException.class,
                () -> service.registerStudentToClass(request)
        );

        verify(classRegistrationRepository, never())
                .save(any(ClassRegistration.class));
        verify(mapper, never()).toResponseDto(any());
    }

    // 5. Cập nhật trạng thái đăng ký thành công
    @Test
    void updateRegistrationStatus_shouldUpdateSuccessfully() {
        Long registrationId = 10L;
        RegistrationStatus newStatus = RegistrationStatus.ACCEPTED;

        ClassRegistration registration = mock(ClassRegistration.class);
        ClassRegistrationResponseDto expected =
                new ClassRegistrationResponseDto();

        when(classRegistrationRepository.findById(registrationId))
                .thenReturn(Optional.of(registration));
        when(classRegistrationRepository.save(registration))
                .thenReturn(registration);
        when(mapper.toResponseDto(registration)).thenReturn(expected);

        ClassRegistrationResponseDto result =
                service.updateRegistrationStatus(registrationId, newStatus);

        assertSame(expected, result);

        verify(registration).setStatus(newStatus);
        verify(registration).setPending(false);
        verify(classRegistrationRepository).save(registration);
        verify(mapper).toResponseDto(registration);
    }

    // 6. Cập nhật thất bại vì không tìm thấy đăng ký
    @Test
    void updateRegistrationStatus_shouldThrowWhenNotFound() {
        Long registrationId = 10L;

        when(classRegistrationRepository.findById(registrationId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.updateRegistrationStatus(
                        registrationId,
                        RegistrationStatus.ACCEPTED
                )
        );

        verify(classRegistrationRepository, never())
                .save(any(ClassRegistration.class));
    }

    // 7. Xóa đăng ký thành công
    @Test
    void deleteRegistration_shouldDeleteSuccessfully() {
        Long registrationId = 10L;
        ClassRegistration registration = new ClassRegistration();

        when(classRegistrationRepository.findById(registrationId))
                .thenReturn(Optional.of(registration));

        assertDoesNotThrow(
                () -> service.deleteRegistration(registrationId)
        );

        verify(classRegistrationRepository).delete(registration);
    }

    // 8. Xóa thất bại vì không tìm thấy đăng ký
    @Test
    void deleteRegistration_shouldThrowWhenNotFound() {
        Long registrationId = 10L;

        when(classRegistrationRepository.findById(registrationId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> service.deleteRegistration(registrationId)
        );

        verify(classRegistrationRepository, never())
                .delete(any(ClassRegistration.class));
    }

    // 9. Người dùng được xem danh sách đăng ký của chính mình
    @Test
    void getRegistrationsByUser_shouldAllowUserToViewOwnRegistrations() {
        Long userId = 5L;
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(userId);
//        when(principal.getAuthorities()).thenReturn(List.of());

        User user = new User();
        ClassRegistration registration = mock(ClassRegistration.class);
        ClassRoom classRoom = mock(ClassRoom.class);

        when(classRoom.getTitle()).thenReturn("Java Backend");
        when(registration.getClassEntity()).thenReturn(classRoom);

        Page<ClassRegistration> page =
                new PageImpl<>(List.of(registration), pageable, 1);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(classRegistrationRepository.findByStudent(user, pageable))
                .thenReturn(page);
        when(registration.getRegistrationId()).thenReturn(100L);

        Page<ClassRegistrationResponseDto> result =
                service.getRegistrationsByUser(userId, pageable, principal);

        assertEquals(1, result.getTotalElements());
        assertEquals(100L, result.getContent().get(0).getRegistrationId());
        assertEquals("Java Backend", result.getContent().get(0).getClassTitle());

        verify(userRepository).findById(userId);
        verify(classRegistrationRepository).findByStudent(user, pageable);
    }

    // 10. ADMIN được xem danh sách đăng ký của người dùng khác
    @Test
    void getRegistrationsByUser_shouldAllowAdminToViewOtherUserRegistrations() {
        Long userId = 5L;
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(99L);
        doReturn(
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        ).when(principal).getAuthorities();

        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(classRegistrationRepository.findByStudent(user, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<ClassRegistrationResponseDto> result =
                service.getRegistrationsByUser(userId, pageable, principal);

        assertTrue(result.isEmpty());

        verify(userRepository).findById(userId);
        verify(classRegistrationRepository).findByStudent(user, pageable);
    }

    // 11. Người dùng không có quyền xem dữ liệu của người khác
    @Test
    void getRegistrationsByUser_shouldThrowForbiddenForOtherUser() {
        Long userId = 5L;
        Pageable pageable = PageRequest.of(0, 10);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(99L);
        when(principal.getAuthorities()).thenReturn(List.of());

        assertThrows(
                ForbiddenException.class,
                () -> service.getRegistrationsByUser(userId, pageable, principal)
        );

        verify(userRepository, never()).findById(any());
        verify(classRegistrationRepository, never())
                .findByStudent(any(User.class), any(Pageable.class));
    }

    // 12. Lấy danh sách đăng ký theo sinh viên và trạng thái
    @Test
    void getByStudentIdAndStatus_shouldReturnMappedPage() {
        Long studentId = 5L;
        RegistrationStatus status = RegistrationStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);

        ClassRegistration registration = new ClassRegistration();
        ClassRegistrationResponseDto expected =
                new ClassRegistrationResponseDto();

        Page<ClassRegistration> page =
                new PageImpl<>(List.of(registration), pageable, 1);

        when(classRegistrationRepository.findByStudent_IdAndStatus(
                pageable, studentId, status
        )).thenReturn(page);

        when(mapper.toResponseDto(registration)).thenReturn(expected);

        Page<ClassRegistrationResponseDto> result =
                service.getByStudentIdAndStatus(pageable, studentId, status);

        assertEquals(1, result.getTotalElements());
        assertSame(expected, result.getContent().get(0));

        verify(classRegistrationRepository)
                .findByStudent_IdAndStatus(pageable, studentId, status);
        verify(mapper).toResponseDto(registration);
    }
}

