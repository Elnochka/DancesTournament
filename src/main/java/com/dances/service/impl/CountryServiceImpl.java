package com.dances.service.impl;

import com.dances.entity.Country;
import com.dances.repository.CountryRepository;
import com.dances.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    public Country createCountry(Country country) {
        if (countryRepository.existsByNameCountry(country.getNameCountry())) {
            throw new IllegalArgumentException("Country with this name already exists");
        }
        return countryRepository.save(country);
    }

    @Override
    public List<Country> getAllCountry() {
        return countryRepository.findAll();
    }

    @Override
    public Optional<Country> getCountryById(Long id) {
        return countryRepository.findById(id);
    }

    @Override
    public Country updateCountry(Long id, Country updatedCountry) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));
        country.setNameCountry(updatedCountry.getNameCountry());
        return countryRepository.save(country);
    }

    @Override
    public void deleteCountry(Long id) {
        countryRepository.deleteById(id);
    }
}
