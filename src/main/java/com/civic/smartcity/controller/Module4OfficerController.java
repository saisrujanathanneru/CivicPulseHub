package com.civic.smartcity.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.security.JwtUtil;
import com.civic.smartcity.service.GrievanceService;

/**
 * Module 4: Department Officer Module
 * 
 * Endpoints for officers to view and manage their assigned grievances.
 */
@RestController
@RequestMapping("/api/module4/grievances")
@CrossOrigin(origins = "*")
public class Module4OfficerController {

    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        return authHeader.substring(7);
    }

    /**
     * GET /api/module4/grievances/assigned
     * 
     * Retrieves grievances assigned to the logged-in officer.
     */
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedToMe(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            String username = jwtUtil.getUsernameFromToken(token);
            return ResponseEntity.ok(grievanceService.getByOfficer(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/module4/grievances/{id}/progress
     * 
     * Officer updates status (usually to IN_PROGRESS) and adds remarks.
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String status = body.get("status");
            String remarks = body.get("remarks");
            GrievanceResponse response = grievanceService.updateStatus(id, status, remarks, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/module4/grievances/{id}/resolve
     * 
     * Officer marks grievance as RESOLVED and uploads a resolution image.
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String remarks = body.get("remarks");
            String imageBase64 = body.get("imageBase64");
            GrievanceResponse response = grievanceService.resolveWithImage(id, remarks, imageBase64, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
