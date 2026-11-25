package com.dances.service;

import com.dances.entity.KindOfDance;

import java.util.List;
import java.util.Optional;

public interface KindOfDanceService {

    KindOfDance create(KindOfDance kindOfDance);
    List<KindOfDance> getAll();
    Optional<KindOfDance> getById(Long id);
    KindOfDance update(Long id, KindOfDance kindOfDance);
    void delete(Long id);
}
