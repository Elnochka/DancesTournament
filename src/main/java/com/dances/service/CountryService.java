package com.dances.service;

import com.dances.entity.Country;

import java.util.List;
import java.util.Optional;

public interface CountryService {

    Country createCountry(Country country);
    List<Country> getAllCountry();
    Optional<Country> getCountryById(Long id);
    Country updateCountry(Long id, Country updatedCountry);
    void deleteCountry(Long id);
}
