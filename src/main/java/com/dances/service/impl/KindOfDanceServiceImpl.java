package com.dances.service.impl;

import com.dances.entity.KindOfDance;
import com.dances.repository.KindOfDanceRepository;
import com.dances.service.KindOfDanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KindOfDanceServiceImpl implements KindOfDanceService {

    private final KindOfDanceRepository kindOfDanceRepository;

    @Override
    public KindOfDance create(KindOfDance kindOfDance) {
        if (kindOfDanceRepository.existsByNameKindOfDance(kindOfDance.getNameKindOfDance())) {
            throw new IllegalArgumentException("Kind of dance with this name already exists");
        }
        return kindOfDanceRepository.save(kindOfDance);
    }

    @Override
    public List<KindOfDance> getAll() {
        return kindOfDanceRepository.findAll();
    }

    @Override
    public Optional<KindOfDance> getById(Long id) {
        return kindOfDanceRepository.findById(id);
    }

    @Override
    public KindOfDance update(Long id, KindOfDance updateKindOfDance) {
        KindOfDance kd = kindOfDanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kind  of dance not found"));
        kd.setNameKindOfDance(updateKindOfDance.getNameKindOfDance());
        kd.setFullNameKindOfDance(updateKindOfDance.getFullNameKindOfDance());
        return kindOfDanceRepository.save(kd);
    }

    @Override
    public void delete(Long id) {
        kindOfDanceRepository.deleteById(id);
    }
}
