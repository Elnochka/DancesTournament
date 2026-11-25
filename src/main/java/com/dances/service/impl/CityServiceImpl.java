package com.dances.service.impl;

import com.dances.entity.City;
import com.dances.repository.CityRepository;
import com.dances.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public City createCity(City city) {
        if (cityRepository.existsByNameCity(city.getNameCity())) {
            throw new IllegalArgumentException("City with this name already exists");
        }
        return cityRepository.save(city);
    }

    @Override
    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    @Override
    public Optional<City> getCityById(Long id) {
        return cityRepository.findById(id);
    }

    @Override
    public City updateCity(Long id, City updatedCity) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("City not found"));
        city.setNameCity(updatedCity.getNameCity());
        return cityRepository.save(city);
    }

    @Override
    public void deleteCity(Long id) {
        cityRepository.deleteById(id);
    }
}
