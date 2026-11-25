package com.dances.service.impl;

import com.dances.entity.CategoryJudge;
import com.dances.repository.CategoryJudgeRepository;
import com.dances.service.CategoryJudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryJudgeServiceImpl implements CategoryJudgeService {

    private final CategoryJudgeRepository repository;

    @Override
    public CategoryJudge create(CategoryJudge categoryJudge) {
        if (repository.existsByNameCategory(categoryJudge.getNameCategory())) {
            throw new IllegalArgumentException("CategoryJudge with this name already exists");
        }
        return repository.save(categoryJudge);
    }

    @Override
    public List<CategoryJudge> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<CategoryJudge> getById(Long id) {
        return repository.findById(id);
    }

    @Override
    public CategoryJudge update(Long id, CategoryJudge updated) {
        CategoryJudge category = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CategoryJudge not found"));
        category.setNameCategory(updated.getNameCategory());
        return repository.save(category);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
