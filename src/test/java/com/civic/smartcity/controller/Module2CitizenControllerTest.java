package com.civic.smartcity.controller;

import com.civic.smartcity.dto.GrievanceRequest;
import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.service.GrievanceService;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class Module2CitizenControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GrievanceService grievanceService;

    @InjectMocks
    private Module2CitizenController controller;

    private static final String AUTH = "Bearer test-token";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void submit_shouldReturn201WithGrievanceResponse() throws Exception {
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

        when(grievanceService.submit(any(GrievanceRequest.class), any(String.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/module2/grievances/submit")
                .header("Authorization", AUTH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Pothole",
                      "description": "Deep hole near market",
                      "category": "ROAD",
                      "location": "Main Street"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void my_shouldReturn200WithCitizenGrievances() throws Exception {
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

        when(grievanceService.getMyGrievances("test-token"))
            .thenReturn(List.of(response));

        mockMvc.perform(get("/api/module2/grievances/my")
                .header("Authorization", AUTH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].citizenUsername").value("citizen_1"));
    }

    @Test
    void getById_shouldReturn200WhenCitizenViewsOwnGrievance() throws Exception {
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

        when(grievanceService.getById(1L, "test-token"))
            .thenReturn(response);

        mockMvc.perform(get("/api/module2/grievances/1")
                .header("Authorization", AUTH))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
