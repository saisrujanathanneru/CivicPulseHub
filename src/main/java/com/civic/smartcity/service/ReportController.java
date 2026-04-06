package com.civic.smartcity.controller;

import com.civic.smartcity.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/category")
    public Map<String, Integer> getCategoryData() {
        return reportService.getComplaintsByCategory();
    }

    @GetMapping("/zones")
    public Map<String, Integer> getZoneData() {
        return reportService.getZoneComplaints();
    }

    @GetMapping("/sla")
    public Map<String, Integer> getSLAData() {
        return reportService.getSLAPerformance();
    }
}
