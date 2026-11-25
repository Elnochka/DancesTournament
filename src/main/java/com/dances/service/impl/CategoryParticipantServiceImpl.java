package com.dances.service.impl;

import com.dances.entity.CategoryParticipant;
import com.dances.repository.CategoryParticipantRepository;
import com.dances.service.CategoryParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryParticipantServiceImpl implements CategoryParticipantService {

    private final CategoryParticipantRepository repository;

    @Override
    public CategoryParticipant create(CategoryParticipant category) {
        if (repository.existsByNameCategory(category.getNameCategory())) {
            throw new IllegalArgumentException("CategoryParticipants with this name already exists");
        }
        return repository.save(category);
    }

    @Override
    public List<CategoryParticipant> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<CategoryParticipant> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public CategoryParticipant update(Long id, CategoryParticipant updated) {
        CategoryParticipant category = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CategoryParticipant not found"));
        category.setNameCategory(updated.getNameCategory());
        category.setAge(updated.getAge());
        category.setAgeon(updated.getAgeon());
        return repository.save(category);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
