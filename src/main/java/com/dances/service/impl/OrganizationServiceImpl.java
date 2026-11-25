package com.dances.service.impl;

import com.dances.entity.Organization;
import com.dances.repository.OrganizationRepository;
import com.dances.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Override
    public Organization createOrganization(Organization organization) {
        if (organizationRepository.existsByNameOrganization(organization.getNameOrganization())) {
            throw new IllegalArgumentException("Organization with this name already exists");
        }
        return organizationRepository.save(organization);
    }

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Override
    public Optional<Organization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    @Override
    public Organization updateOrganization(Long id, Organization updatedOrganization) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));
        organization.setNameOrganization(updatedOrganization.getNameOrganization());
        return organizationRepository.save(organization);
    }

    @Override
    public void deleteOrganization(Long id) {
        organizationRepository.deleteById(id);
    }
}
