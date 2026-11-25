package com.dances.service;

import com.dances.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationService {

    Organization createOrganization(Organization organization);
    List<Organization> getAllOrganizations();
    Optional<Organization> getOrganizationById(Long id);
    Organization updateOrganization(Long id, Organization updatedOrganization);
    void deleteOrganization(Long id);
}
