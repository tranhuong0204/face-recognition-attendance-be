package com.example.be.web.controller;

import com.example.be.web.base.VsResponseUtil;
import com.example.be.web.constant.ErrorMessage;
import com.example.be.web.doman.dto.request.classRoom.ClassRegistrationRequestDto;
import com.example.be.web.doman.dto.response.classRoom.ClassRegistrationResponseDto;
import com.example.be.web.doman.model.RegistrationStatus;
import com.example.be.web.security.CurrentUser;
import com.example.be.web.security.UserPrincipal;
import com.example.be.web.service.ClassRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/class-registrations")
@RequiredArgsConstructor
public class ClassRegistrationController {
    private final ClassRegistrationService classRegistrationService;

    @Tag(name = "Class Registration")
    @PreAuthorize("hasAnyRole('USER')")
    @PostMapping
    @Operation(summary = "API register class", description = "User")
    public ResponseEntity<ClassRegistrationResponseDto> registerClass(
            @RequestBody ClassRegistrationRequestDto requestDto
            ) {
        ClassRegistrationResponseDto responseDto = classRegistrationService.registerStudentToClass(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Tag(name = "Class Registration")
    @PutMapping("/{registrationId}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "API update registration status", description = "Admin / Leader")
    public ResponseEntity<ClassRegistrationResponseDto> updateRegistrationStatus(
            @PathVariable Long registrationId,
            @RequestParam("status") RegistrationStatus status
    ) {
        ClassRegistrationResponseDto responseDto = classRegistrationService.updateRegistrationStatus(registrationId, status);
        return ResponseEntity.ok(responseDto);
    }

    @Tag(name = "Class Registration")
    @DeleteMapping("/{registrationId}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "API delete registration", description = "Admin / Leader")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long registrationId) {
        classRegistrationService.deleteRegistration(registrationId);
        return ResponseEntity.noContent().build();
    }

    @Tag(name = "Class Registration")
    @GetMapping("/{studentId}/classes")
    @PreAuthorize("hasAnyRole('USER')")
    @Operation(summary = "lấy danh sách lớp đã đăng ký của sinh viên", description = "User")
    public Page<ClassRegistrationResponseDto> getRegisteredClassesForStudent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long studentId) {
        Pageable pageable = PageRequest.of(page, size);
        return classRegistrationService.getByStudentId(pageable, studentId);
    }

    @Tag(name = "Class Registration")
    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "lấy danh sách sinh viên đã đăng ký của lớp học", description = "Admin / Leader")
    public Page<ClassRegistrationResponseDto> getRegisteredStudentsForClass(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long classId) {
        Pageable pageable = PageRequest.of(page, size);
        return classRegistrationService.getByClassId(pageable, classId);
    }

    @Tag(name = "class-registration-MEMBER-controller")
    @Operation(summary = "View the registered classes", description = "Member")
    @GetMapping("/registration/view")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyRegistrations(@Parameter(hidden = true) @CurrentUser UserPrincipal user,
                                                @ParameterObject Pageable pageable) {
        try {
            return VsResponseUtil.success(classRegistrationService.getRegistrationsByUser(user.getId(), pageable, user));
        } catch (Exception e) {
            return VsResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ClassRegistration.GET_MY_REGISTRATIONS_FAILED);
        }
    }

    @Tag(name = "Class Registration")
    @GetMapping("/{classId}/students/status")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    @Operation(summary = "lấy danh sách sinh viên đã đăng ký của lớp học theo trạng thái đăng ký", description = "Admin / Leader")
    public Page<ClassRegistrationResponseDto> getRegisteredStudentsForClassByStatus(
            @ParameterObject Pageable pageable,
            @PathVariable Long classId,
            @RequestParam("status") RegistrationStatus status) {
        return classRegistrationService.getByClassIdAndStatus(pageable, classId, status);
    }
}
