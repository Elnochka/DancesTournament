package com.dances.service;

import com.dances.entity.CategoryParticipant;

import java.util.List;
import java.util.Optional;

public interface CategoryParticipantService {

    CategoryParticipant create(CategoryParticipant category);
    List<CategoryParticipant> getAll();
    Optional<CategoryParticipant> getById(Long id);
    CategoryParticipant update(Long id, CategoryParticipant category);
    void delete(Long id);
}
