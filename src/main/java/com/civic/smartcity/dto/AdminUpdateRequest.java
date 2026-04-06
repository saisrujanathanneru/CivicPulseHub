package com.civic.smartcity.dto;
import lombok.Data;
@Data
public class AdminUpdateRequest {
    private String status;
    private String assignedOfficer;
    private String remarks;
    private String priority;
    private String deadline;

   }