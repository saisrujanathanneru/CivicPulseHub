package com.civic.smartcity.repository;

import com.civic.smartcity.model.Grievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface GrievanceRepository extends JpaRepository<Grievance, Long>, JpaSpecificationExecutor<Grievance> {
    List<Grievance> findByCitizenUsernameOrderBySubmittedAtDesc(String citizenUsername);
    List<Grievance> findAllByOrderBySubmittedAtDesc();
    List<Grievance> findByStatusOrderBySubmittedAtDesc(String status);
    List<Grievance> findByAssignedOfficerOrderBySubmittedAtDesc(String officer);
}
