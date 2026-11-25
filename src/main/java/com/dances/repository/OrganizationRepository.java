package com.dances.repository;

import com.dances.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByNameOrganization(String name);
}
