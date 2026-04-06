package com.civic.smartcity.security;

import com.civic.smartcity.service.GrievanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Module_3_SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrievanceService grievanceService;

    @Test
    @WithMockUser(username = "citizen_1", roles = {"CITIZEN"})
    void all_shouldRejectCitizenRole() throws Exception {
        mockMvc.perform(get("/api/module3/grievances/all")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer_1", roles = {"OFFICER"})
    void officers_shouldRejectOfficerRole() throws Exception {
        mockMvc.perform(get("/api/module3/grievances/officers")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "officer_1", roles = {"OFFICER"})
    void all_shouldAllowOfficerRole() throws Exception {
        when(grievanceService.getAll("token")).thenReturn(List.of());

        mockMvc.perform(get("/api/module3/grievances/all")
                .header("Authorization", "Bearer token"))
            .andExpect(status().isOk());
    }
}
