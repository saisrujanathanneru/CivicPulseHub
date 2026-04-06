package com.civic.smartcity.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.service.GrievanceService;

/**
 * Module 5: Feedback & Rating System
 * 
 * Endpoints for citizens to rate resolved grievances and reopen them if unsatisfied.
 */
@RestController
@RequestMapping("/api/module5/grievances")
@CrossOrigin(origins = "*")
public class Module5FeedbackController {

    @Autowired
    private GrievanceService grievanceService;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        return authHeader.substring(7);
    }

    /**
     * POST /api/module5/grievances/{id}/feedback
     * 
     * Citizen submits a rating and feedback for a resolved grievance.
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> submitFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Integer rating = (Integer) body.get("rating");
            String feedback = (String) body.get("feedback");
            GrievanceResponse response = grievanceService.submitFeedback(id, rating, feedback, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/module5/grievances/{id}/reopen
     * 
     * Citizen reopens a resolved or closed grievance.
     */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<?> reopen(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GrievanceResponse response = grievanceService.reopenGrievance(id, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
