package com.dances.repository;

import com.dances.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {

    boolean existsByNameCity(String name);
}
