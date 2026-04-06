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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.civic.smartcity.dto.AdminAssignRequest;
import com.civic.smartcity.dto.AdminUpdateRequest;
import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.service.GrievanceService;

/**
 * Module 3: Admin & Officer Grievance Management
 * 
 * Endpoints for administrators and officers to manage, filter, assign, and resolve grievances.
 * Access control enforced at security config level (ADMIN/OFFICER roles required).
 */
@RestController
@RequestMapping("/api/module3/grievances")
@CrossOrigin(origins = "*")
public class Module3AdminOfficerController {

    @Autowired
    private GrievanceService grievanceService;

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid Authorization header.");
        return authHeader.substring(7);
    }

    /**
     * GET /api/module3/grievances/all
     * 
     * Admin/Officer retrieves all grievances in the system.
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAll(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            List<GrievanceResponse> list = grievanceService.getAll(token);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/module3/grievances/filter?status=PENDING
     * 
     * Admin/Officer filters grievances by status (PENDING, IN_PROGRESS, RESOLVED, CLOSED).
     */
    @GetMapping("/filter")
    public ResponseEntity<?> filterByStatus(
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            return ResponseEntity.ok(grievanceService.getByStatus(status, token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/module3/grievances/stats
     * 
     * Admin/Officer retrieves aggregated statistics (total, pending, resolved, by priority, etc.).
     */
    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getStats(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/module3/grievances/officers
     * 
     * Admin-only: Retrieves list of all available officers for assignment.
     */
    @GetMapping("/officers")
    public ResponseEntity<?> officers(
            @RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(grievanceService.getOfficerUsernames(extractToken(authHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/module3/grievances/admin/assign
     * 
     * Admin-only: Assigns a grievance to an officer with department, priority, and deadline.
     */
    @PostMapping("/admin/assign")
    public ResponseEntity<?> adminAssign(
            @RequestBody AdminAssignRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GrievanceResponse response = grievanceService.adminAssign(request, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/module3/grievances/admin/list
     * 
     * Admin/Officer retrieves paginated list of grievances with filtering by status, priority, category, and search.
     */
    @GetMapping("/admin/list")
    public ResponseEntity<?> adminList(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        try {
            return ResponseEntity.ok(grievanceService.getAdminList(
                extractToken(authHeader), page, size, status, priority, category, search
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/module3/grievances/{id}/status
     * 
     * Admin/Officer updates the status of a grievance with optional remarks.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String status  = body.get("status");
            String remarks = body.get("remarks");
            GrievanceResponse response = grievanceService.updateStatus(id, status, remarks, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/module3/grievances/{id}/admin-update
     * 
     * Admin-only: Updates all grievance details (status, priority, officer, deadline, remarks).
     */
    @PutMapping("/{id}/admin-update")
    public ResponseEntity<?> adminUpdate(
            @PathVariable Long id,
            @RequestBody AdminUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            GrievanceResponse response = grievanceService.adminUpdate(id, request, extractToken(authHeader));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
