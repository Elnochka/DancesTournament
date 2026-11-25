package com.dances.service;

import com.dances.entity.Klass;

import java.util.List;
import java.util.Optional;

public interface KlassService {

    Klass createKlass(Klass klass);
    List<Klass> getAllKlasses();
    Optional<Klass> getKlassById(Long id);
    Klass updateKlass(Long id, Klass updatedKlass);
    void deleteKlass(Long id);
}
