package com.dances.service.impl;

import com.dances.entity.CategoryTournament;
import com.dances.entity.Klass;
import com.dances.entity.RoundSettings;
import com.dances.entity.dto.RoundSettingsDto;
import com.dances.exception.NotFoundException;
import com.dances.model.RoundEnum;
import com.dances.repository.CategoryTournamentRepository;
import com.dances.repository.KlassRepository;
import com.dances.repository.ParticipantTournamentRepository;
import com.dances.repository.RoundSettingsRepository;
import com.dances.service.KlassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KlassServiceImpl implements KlassService {

    private final KlassRepository klassRepository;

    @Override
    public Klass createKlass(Klass klass) {
        if (klassRepository.existsByNameKlass(klass.getNameKlass())) {
            throw new IllegalArgumentException("Klass with this name already exists");
        }
        return klassRepository.save(klass);
    }

    @Override
    public List<Klass> getAllKlasses() {
        return klassRepository.findAll();
    }

    @Override
    public Optional<Klass> getKlassById(Long id) {
        return klassRepository.findById(id);
    }

    @Override
    public Klass updateKlass(Long id, Klass updatedKlass) {
        Klass klass = klassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Klass not found"));
        klass.setNameKlass(updatedKlass.getNameKlass());
        return klassRepository.save(klass);
    }

    @Override
    public void deleteKlass(Long id) {
        klassRepository.deleteById(id);
    }

}
