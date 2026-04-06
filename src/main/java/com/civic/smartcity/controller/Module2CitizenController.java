package com.civic.smartcity.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.smartcity.dto.GrievanceRequest;
import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.service.GrievanceService;

/**
 * Module 2: Citizen Grievance Submission & Tracking
 * 
 * Endpoints for citizens to submit grievances and view their own submissions.
 * Access control enforced at security config level (CITIZEN role required).
 */
@RestController
@RequestMapping("/api/module2/grievances")
@CrossOrigin(origins = "*")
public class Module2CitizenController {

    @Autowired
    private GrievanceService grievanceService;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        return authHeader.substring(7);
    }

    /**
     * POST /api/module2/grievances/submit
     * 
     * Citizen submits a new grievance with title, description, category, location, and optional image.
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestBody GrievanceRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GrievanceResponse response = grievanceService.submit(request, extractToken(authHeader));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/module2/grievances/my
     * 
     * Citizen retrieves all their submitted grievances in descending order by submission date.
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyGrievances(
            @RequestHeader("Authorization") String authHeader) {
        try {
            List<GrievanceResponse> list = grievanceService.getMyGrievances(extractToken(authHeader));
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/module2/grievances/{id}
     * 
     * Citizen retrieves details of their specific grievance by ID.
     * Citizens can only view their own grievances; access is restricted via service.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getById(id, extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
