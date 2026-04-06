package com.civic.smartcity.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ReportService {

    public Map<String, Integer> getComplaintsByCategory() {
        Map<String, Integer> data = new HashMap<>();
        data.put("Road Issues", 10);
        data.put("Water Issues", 7);
        data.put("Electricity", 5);
        return data;
    }

    public Map<String, Integer> getZoneComplaints() {
        Map<String, Integer> data = new HashMap<>();
        data.put("Zone A", 8);
        data.put("Zone B", 12);
        data.put("Zone C", 4);
        return data;
    }

    public Map<String, Integer> getSLAPerformance() {
        Map<String, Integer> data = new HashMap<>();
        data.put("Within SLA", 15);
        data.put("Delayed", 9);
        return data;
    }
}
