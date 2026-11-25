package com.dances.repository;

import com.dances.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {

    boolean existsByNameCountry(String name);
}
