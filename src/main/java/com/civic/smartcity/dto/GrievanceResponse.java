package com.civic.smartcity.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GrievanceResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String location;
    private String imageBase64;
    private String citizenUsername;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private String assignedOfficer;
    private String remarks;
    // Module 3
    private String priority;
    private LocalDateTime deadline;
    private String department;
    // Module 4
    private String resolutionImageBase64;
    // Module 5
    private Integer rating;
    private String feedback;
}
