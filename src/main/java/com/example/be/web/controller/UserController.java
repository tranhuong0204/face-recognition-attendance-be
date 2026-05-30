package com.example.be.web.controller;

import com.example.be.web.base.RestApiV1;
import com.example.be.web.base.VsResponseUtil;
import com.example.be.web.constant.ResponseMessage;
import com.example.be.web.constant.UrlConstant;
import com.example.be.web.doman.dto.request.user.UserCreateDto;
import com.example.be.web.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RestApiV1
@Validated

public class UserController {

    private final UserService userService;

    @Tag(name="User_Controller")
    @Operation(summary = "Get user by id", description = "Admin / Leader")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId){
        return VsResponseUtil.success(userService.getUserById(userId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    @Tag(name="User_Controller")
    @Operation(summary = "create users")
    @PostMapping(UrlConstant.User.CREATE_USER)
    public ResponseEntity<?> createUser(@RequestBody @Valid UserCreateDto createDto){
        return VsResponseUtil.success(userService.createUser(createDto));
    }

    @Tag(name="User_Controller")
    @DeleteMapping(UrlConstant.User.DELETE_USER)
    @Operation(summary = "Delete user by id", description = "Admin / Leader")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUsers(id);
        return VsResponseUtil.success(ResponseMessage.User.USER_DELETE);
    }

}
