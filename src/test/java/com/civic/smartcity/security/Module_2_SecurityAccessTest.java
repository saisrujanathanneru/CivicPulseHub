package com.civic.smartcity.security;

import com.civic.smartcity.service.GrievanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Module_2_SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrievanceService grievanceService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void submit_shouldRejectNonCitizenRole() throws Exception {
        mockMvc.perform(post("/api/module2/grievances/submit")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title": "Broken road",
                      "description": "Pothole near bus stand",
                      "category": "ROAD"
                    }
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void my_shouldRejectNonCitizenRole() throws Exception {
        mockMvc.perform(get("/api/module2/grievances/my")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "citizen_1", roles = {"CITIZEN"})
    void my_shouldAllowCitizenRole() throws Exception {
        when(grievanceService.getMyGrievances("token")).thenReturn(List.of());

        mockMvc.perform(get("/api/module2/grievances/my")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk());
    }
}
