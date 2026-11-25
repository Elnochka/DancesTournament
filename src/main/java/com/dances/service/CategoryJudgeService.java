package com.dances.service;

import com.dances.entity.CategoryJudge;

import java.util.List;
import java.util.Optional;

public interface CategoryJudgeService {

    CategoryJudge create(CategoryJudge categoryJudge);
    List<CategoryJudge> getAll();
    Optional<CategoryJudge> getById(Long id);
    CategoryJudge update(Long id, CategoryJudge categoryJudge);
    void delete(Long id);
}
