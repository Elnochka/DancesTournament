package com.dances.repository;

import com.dances.entity.CategoryJudge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJudgeRepository extends JpaRepository<CategoryJudge, Long> {

    boolean existsByNameCategory(String nameCategory);
}
