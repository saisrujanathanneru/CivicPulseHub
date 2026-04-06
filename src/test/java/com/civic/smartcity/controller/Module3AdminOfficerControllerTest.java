package com.civic.smartcity.controller;

import com.civic.smartcity.dto.AdminAssignRequest;
import com.civic.smartcity.dto.AdminUpdateRequest;
import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.service.GrievanceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class Module3AdminOfficerControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GrievanceService grievanceService;

    @InjectMocks
    private Module3AdminOfficerController controller;

    private static final String AUTH = "Bearer test-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void all_shouldReturn200AndAllGrievances() throws Exception {
        GrievanceResponse response = new GrievanceResponse(
            1L,
            "Pothole",
            "Deep hole near market",
            "ROAD",
            "PENDING",
            "Main Street",
            null,
            "citizen_1",
            LocalDateTime.now(),
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(grievanceService.getAll("test-token")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/module3/grievances/all")
                .header("Authorization", AUTH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void filter_shouldReturn200AndFilteredGrievances() throws Exception {
        GrievanceResponse response = new GrievanceResponse(
            2L,
            "Overflowing drain",
            "Water logging near school",
            "DRAINAGE",
            "IN_PROGRESS",
            "Sector 4",
            null,
            "citizen_2",
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now().minusHours(5),
            "officer_anna",
            "Team assigned",
            "HIGH",
            null,
            "Public Works"
        );

        when(grievanceService.getByStatus("IN_PROGRESS", "test-token"))
            .thenReturn(List.of(response));

        mockMvc.perform(get("/api/module3/grievances/filter")
                .header("Authorization", AUTH)
                .queryParam("status", "IN_PROGRESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    void stats_shouldReturn200AndStatistics() throws Exception {
        when(grievanceService.getStats("test-token")).thenReturn(Map.of(
            "total", 10L,
            "pending", 4L,
            "inProgress", 3L,
            "resolved", 2L,
            "critical", 1L
        ));

        mockMvc.perform(get("/api/module3/grievances/stats")
                .header("Authorization", AUTH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(10))
            .andExpect(jsonPath("$.pending").value(4));
    }

    @Test
    void officers_shouldReturn200AndOfficerList() throws Exception {
        when(grievanceService.getOfficerUsernames("test-token"))
            .thenReturn(List.of("officer_anna", "officer_john"));

        mockMvc.perform(get("/api/module3/grievances/officers")
                .header("Authorization", AUTH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("officer_anna"))
            .andExpect(jsonPath("$[1]").value("officer_john"));
    }

    @Test
    void adminAssign_shouldReturn200WithAssignedGrievance() throws Exception {
        GrievanceResponse response = new GrievanceResponse(
            99L,
            "Water leak",
            "Pipe burst near market",
            "WATER",
            "IN_PROGRESS",
            "Main Street",
            null,
            "citizen_1",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now(),
            "officer_john",
            "Assigned",
            "HIGH",
            LocalDateTime.now().plusDays(2),
            "Water Supply"
        );

        when(grievanceService.adminAssign(any(AdminAssignRequest.class), anyString()))
            .thenReturn(response);

        mockMvc.perform(post("/api/module3/grievances/admin/assign")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "grievanceId": 99,
                      "assignedOfficer": "officer_john",
                      "priority": "HIGH"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignedOfficer").value("officer_john"))
            .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void updateStatus_shouldReturn200WithUpdatedStatus() throws Exception {
        GrievanceResponse response = new GrievanceResponse(
            1L,
            "Pothole",
            "Deep hole near market",
            "ROAD",
            "RESOLVED",
            "Main Street",
            null,
            "citizen_1",
            LocalDateTime.now().minusDays(5),
            LocalDateTime.now(),
            "officer_john",
            "Fixed",
            "MEDIUM",
            null,
            "Public Works"
        );

        when(grievanceService.updateStatus(eq(1L), eq("RESOLVED"), anyString(), anyString()))
            .thenReturn(response);

        mockMvc.perform(put("/api/module3/grievances/1/status")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status": "RESOLVED",
                      "remarks": "Fixed"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RESOLVED"));
    }

    @Test
    void adminUpdate_shouldReturn200WithUpdatedGrievance() throws Exception {
        GrievanceResponse response = new GrievanceResponse(
            99L,
            "Water leak",
            "Pipe burst near market",
            "WATER",
            "IN_PROGRESS",
            "Main Street",
            null,
            "citizen_1",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now(),
            "officer_john",
            "Assigned",
            "HIGH",
            LocalDateTime.now().plusDays(2),
            "Water Supply"
        );

        when(grievanceService.adminUpdate(eq(99L), any(AdminUpdateRequest.class), anyString()))
            .thenReturn(response);

        mockMvc.perform(put("/api/module3/grievances/99/admin-update")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "status":"IN_PROGRESS",
                      "assignedOfficer":"officer_john",
                      "priority":"HIGH",
                      "remarks":"Assigned"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.assignedOfficer").value("officer_john"));
    }
}
