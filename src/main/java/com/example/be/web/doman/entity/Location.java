package com.example.be.web.doman.entity;

import com.example.be.web.doman.model.RecordStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor

@Setter
@Getter
@Entity
@Builder
@Table(name = "location")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @Column(name = "location_code", nullable = false, length = 100)
    private String locationCode;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
        private Double longitude;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "radius_meters")
    private Double radiusMeters;

    // Một địa điểm có thể thuộc nhiều lớp
    @ManyToMany(mappedBy = "locations")
    @JsonIgnore
    private Set<ClassRoom> classRooms = new HashSet<>();

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<AttendanceSession> attendanceSessions = new HashSet<>();

}
