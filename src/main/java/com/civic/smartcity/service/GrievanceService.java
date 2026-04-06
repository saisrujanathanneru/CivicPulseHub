package com.civic.smartcity.service;

import com.civic.smartcity.dto.AdminAssignRequest;
import com.civic.smartcity.dto.AdminUpdateRequest;
import com.civic.smartcity.dto.GrievanceRequest;
import com.civic.smartcity.dto.GrievanceResponse;
import com.civic.smartcity.model.Grievance;
import com.civic.smartcity.model.User;
import com.civic.smartcity.repository.GrievanceRepository;
import com.civic.smartcity.repository.UserRepository;
import com.civic.smartcity.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GrievanceService {

    @Autowired
    private GrievanceRepository grievanceRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    private static final List<String> VALID_CATEGORIES = List.of(
        "WATER", "STREET_LIGHT", "ROAD", "SANITATION", "DRAINAGE", "PARK", "ELECTRICITY", "OTHER"
    );
    private static final List<String> VALID_PRIORITIES = List.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
    private static final List<String> VALID_STATUSES   = List.of("PENDING", "IN_PROGRESS", "RESOLVED", "CLOSED");

    private void requireRole(String token, String... allowedRoles) {
        String role = jwtUtil.getRoleFromToken(token);
        if (Arrays.stream(allowedRoles).noneMatch(allowed -> allowed.equalsIgnoreCase(role))) {
            throw new IllegalArgumentException("Unauthorized.");
        }
    }

    public GrievanceResponse submit(GrievanceRequest request, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        String category = request.getCategory() != null ? request.getCategory().toUpperCase() : "OTHER";
        if (!VALID_CATEGORIES.contains(category)) throw new IllegalArgumentException("Invalid category.");

        Grievance g = new Grievance();
        g.setTitle(request.getTitle());
        g.setDescription(request.getDescription());
        g.setCategory(category);
        g.setStatus("PENDING");
        g.setLocation(request.getLocation());
        g.setImageBase64(request.getImageBase64());
        g.setCitizenUsername(username);
        g.setSubmittedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public List<GrievanceResponse> getMyGrievances(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return grievanceRepository.findByCitizenUsernameOrderBySubmittedAtDesc(username)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public GrievanceResponse getById(Long id, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        String role     = jwtUtil.getRoleFromToken(token);
        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));
        if (role.equals("CITIZEN") && !g.getCitizenUsername().equals(username))
            throw new IllegalArgumentException("Access denied.");
        return toResponse(g);
    }

    public List<GrievanceResponse> getAll(String token) {
        requireRole(token, "ADMIN", "OFFICER");
        return grievanceRepository.findAllByOrderBySubmittedAtDesc()
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GrievanceResponse> getByStatus(String status, String token) {
        requireRole(token, "ADMIN", "OFFICER");
        String normalizedStatus = status.toUpperCase();
        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid status.");
        }
        return grievanceRepository.findByStatusOrderBySubmittedAtDesc(normalizedStatus)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GrievanceResponse> getByOfficer(String officer) {
        return grievanceRepository.findByAssignedOfficerOrderBySubmittedAtDesc(officer)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public GrievanceResponse adminAssign(AdminAssignRequest request, String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) throw new IllegalArgumentException("Only admins can assign grievances.");

        Grievance g = grievanceRepository.findById(request.getGrievanceId())
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));

        if (request.getAssignedOfficer() != null) {
            String officer = request.getAssignedOfficer().trim();
            if (officer.isEmpty()) {
                g.setAssignedOfficer(null);
            } else {
                User foundOfficer = userRepository.findByUsername(officer)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned officer does not exist."));
                if (!"OFFICER".equalsIgnoreCase(foundOfficer.getRole())) {
                    throw new IllegalArgumentException("Assigned user is not an officer.");
                }
                g.setAssignedOfficer(officer);
            }
        }
        if (request.getDepartment() != null) {
            String department = request.getDepartment().trim();
            g.setDepartment(department.isEmpty() ? null : department);
        }
        if (request.getPriority() != null) {
            String p = request.getPriority().toUpperCase();
            if (!VALID_PRIORITIES.contains(p)) throw new IllegalArgumentException("Invalid priority.");
            g.setPriority(p);
        }
        if (request.getDeadline() != null) g.setDeadline(request.getDeadline());
        if (request.getStatus() != null) {
            String s = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(s)) throw new IllegalArgumentException("Invalid status.");
            g.setStatus(s);
        }
        if (request.getRemarks() != null) g.setRemarks(request.getRemarks());

        if (g.getAssignedOfficer() != null && "PENDING".equals(g.getStatus()))
            g.setStatus("IN_PROGRESS");

        g.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public GrievanceResponse updateStatus(Long id, String status, String remarks, String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role) && !"OFFICER".equals(role)) throw new IllegalArgumentException("Unauthorized.");
        
        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));
            
        if ("OFFICER".equals(role) && !g.getAssignedOfficer().equals(jwtUtil.getUsernameFromToken(token))) {
            throw new IllegalArgumentException("You can only update your assigned grievances.");
        }

        String s = status.toUpperCase();
        if (!VALID_STATUSES.contains(s)) throw new IllegalArgumentException("Invalid status.");
        
        g.setStatus(s);
        if (remarks != null) g.setRemarks(remarks);
        g.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public GrievanceResponse resolveWithImage(Long id, String remarks, String imageBase64, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        String role     = jwtUtil.getRoleFromToken(token);
        if (!"OFFICER".equals(role) && !"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Unauthorized.");
        }

        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));

        if ("OFFICER".equals(role) && !username.equals(g.getAssignedOfficer())) {
            throw new IllegalArgumentException("You are not assigned to this grievance.");
        }

        g.setStatus("RESOLVED");
        g.setRemarks(remarks);
        g.setResolutionImageBase64(imageBase64);
        g.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public GrievanceResponse adminUpdate(Long id, AdminUpdateRequest request, String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Only admins can update grievance assignment details.");
        }

        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String s = request.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(s)) {
                throw new IllegalArgumentException("Invalid status.");
            }
            g.setStatus(s);
        }

        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            String p = request.getPriority().toUpperCase();
            if (!VALID_PRIORITIES.contains(p)) {
                throw new IllegalArgumentException("Invalid priority.");
            }
            g.setPriority(p);
        }

        if (request.getAssignedOfficer() != null) {
            String officer = request.getAssignedOfficer().trim();
            if (officer.isEmpty()) {
                g.setAssignedOfficer(null);
            } else {
                User foundOfficer = userRepository.findByUsername(officer)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned officer does not exist."));
                if (!"OFFICER".equalsIgnoreCase(foundOfficer.getRole())) {
                    throw new IllegalArgumentException("Assigned user is not an officer.");
                }
                g.setAssignedOfficer(officer);
                if ("PENDING".equals(g.getStatus())) {
                    g.setStatus("IN_PROGRESS");
                }
            }
        }

        if (request.getRemarks() != null) {
            String remarks = request.getRemarks().trim();
            g.setRemarks(remarks.isEmpty() ? null : remarks);
        }

        if (request.getDeadline() != null) {
            String deadline = request.getDeadline().trim();
            if (deadline.isEmpty()) {
                g.setDeadline(null);
            } else {
                try {
                    g.setDeadline(LocalDateTime.parse(deadline));
                } catch (DateTimeParseException ex) {
                    throw new IllegalArgumentException("Invalid deadline format.");
                }
            }
        }

        g.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public Map<String, Long> getStats(String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role) && !"OFFICER".equals(role)) {
            throw new IllegalArgumentException("Unauthorized.");
        }

        List<Grievance> all = grievanceRepository.findAll();
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) all.size());
        stats.put("pending", all.stream().filter(g -> "PENDING".equals(g.getStatus())).count());
        stats.put("inProgress", all.stream().filter(g -> "IN_PROGRESS".equals(g.getStatus())).count());
        stats.put("resolved", all.stream().filter(g -> "RESOLVED".equals(g.getStatus())).count());
        stats.put("closed", all.stream().filter(g -> "CLOSED".equals(g.getStatus())).count());
        stats.put("low", all.stream().filter(g -> "LOW".equals(g.getPriority())).count());
        stats.put("medium", all.stream().filter(g -> "MEDIUM".equals(g.getPriority())).count());
        stats.put("high", all.stream().filter(g -> "HIGH".equals(g.getPriority())).count());
        stats.put("critical", all.stream().filter(g -> "CRITICAL".equals(g.getPriority())).count());
        return stats;
    }

    public GrievanceResponse submitFeedback(Long id, Integer rating, String feedback, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));

        if (!g.getCitizenUsername().equals(username)) {
            throw new IllegalArgumentException("You can only provide feedback for your own grievances.");
        }

        if (!"RESOLVED".equals(g.getStatus())) {
            throw new IllegalArgumentException("Feedback can only be provided for resolved grievances.");
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        g.setRating(rating);
        g.setFeedback(feedback);
        g.setStatus("CLOSED");
        g.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public GrievanceResponse reopenGrievance(Long id, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        Grievance g = grievanceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grievance not found."));

        if (!g.getCitizenUsername().equals(username)) {
            throw new IllegalArgumentException("You can only reopen your own grievances.");
        }

        if (!"RESOLVED".equals(g.getStatus()) && !"CLOSED".equals(g.getStatus())) {
            throw new IllegalArgumentException("Only resolved or closed grievances can be reopened.");
        }

        g.setStatus("IN_PROGRESS");
        g.setRemarks(g.getRemarks() + " [REOPENED BY CITIZEN]");
        g.setUpdatedAt(LocalDateTime.now());
        grievanceRepository.save(g);
        return toResponse(g);
    }

    public List<String> getOfficerUsernames(String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role)) {
            throw new IllegalArgumentException("Only admins can view officer list.");
        }

        return userRepository.findByRole("OFFICER")
            .stream()
            .map(User::getUsername)
            .sorted(String::compareToIgnoreCase)
            .collect(Collectors.toList());
    }

    public Map<String, Object> getAdminList(
            String token,
            int page,
            int size,
            String status,
            String priority,
            String category,
            String search) {
        String role = jwtUtil.getRoleFromToken(token);
        if (!"ADMIN".equals(role) && !"OFFICER".equals(role)) {
            throw new IllegalArgumentException("Unauthorized.");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "submittedAt"));

        Specification<Grievance> spec = Specification.where(null);

        if (status != null && !status.isBlank()) {
            String s = status.trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), s));
        }
        if (priority != null && !priority.isBlank()) {
            String p = priority.trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), p));
        }
        if (category != null && !category.isBlank()) {
            String c = category.trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), c));
        }
        if (search != null && !search.isBlank()) {
            String q = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), q),
                cb.like(cb.lower(root.get("description")), q),
                cb.like(cb.lower(root.get("citizenUsername")), q),
                cb.like(cb.lower(cb.coalesce(root.get("assignedOfficer"), "")), q),
                cb.like(cb.lower(root.get("category")), q)
            ));
        }

        Page<Grievance> grievancePage = grievanceRepository.findAll(spec, pageable);
        List<GrievanceResponse> content = grievancePage.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("page", grievancePage.getNumber());
        response.put("size", grievancePage.getSize());
        response.put("totalElements", grievancePage.getTotalElements());
        response.put("totalPages", grievancePage.getTotalPages());
        response.put("first", grievancePage.isFirst());
        response.put("last", grievancePage.isLast());
        return response;
    }

    private GrievanceResponse toResponse(Grievance g) {
        return new GrievanceResponse(
            g.getId(), g.getTitle(), g.getDescription(),
            g.getCategory(), g.getStatus(), g.getLocation(),
            g.getImageBase64(), g.getCitizenUsername(),
            g.getSubmittedAt(), g.getUpdatedAt(),
            g.getAssignedOfficer(), g.getRemarks(),
            g.getPriority(), g.getDeadline(), g.getDepartment(),
            g.getResolutionImageBase64(),
            g.getRating(), g.getFeedback()
        );
    }
}
