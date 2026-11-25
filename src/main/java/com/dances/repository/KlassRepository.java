package com.dances.repository;

import com.dances.entity.Klass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KlassRepository extends JpaRepository<Klass, Long> {

    boolean existsByNameKlass(String name);
}
