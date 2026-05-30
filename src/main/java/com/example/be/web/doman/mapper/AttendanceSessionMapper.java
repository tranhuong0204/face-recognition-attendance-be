package com.example.be.web.doman.mapper;

import com.example.be.web.doman.dto.request.attendance.AttendanceSessionRequestDto;
import com.example.be.web.doman.dto.response.attendance.AttendanceSessionResponseDto;
import com.example.be.web.doman.entity.AttendanceSession;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AttendanceSessionMapper {

    @Mapping(target = "sessionId", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "attendanceRecords", ignore = true)
    @Mapping(target = "classRoom", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "updateAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    AttendanceSession toEntity(AttendanceSessionRequestDto request);

    @Mapping(source = "sessionId", target = "sessionId")
    @Mapping(source = "createdByUser.id", target = "createdByUserId")
    @Mapping(source = "location.locationId", target = "locationId")
    @Mapping(source = "classRoom.classId", target = "classroomId")

    @Mapping(source = "classRoom.title", target = "classroomTitle")

    AttendanceSessionResponseDto toResponse(AttendanceSession entity);

    void updateEntityFromDto(
            AttendanceSessionRequestDto dto,
            @MappingTarget AttendanceSession entity
    );

    List<AttendanceSessionResponseDto> toResponses(List<AttendanceSession> entities);
}