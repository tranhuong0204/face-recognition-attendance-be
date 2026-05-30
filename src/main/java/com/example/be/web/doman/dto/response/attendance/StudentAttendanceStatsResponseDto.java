package com.example.be.web.doman.dto.response.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAttendanceStatsResponseDto {
    private Long studentId;
    private String studentName;
    private long present;
    private long absent;
    private double percent;
}